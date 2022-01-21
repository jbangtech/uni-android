package com.jbangit.pushdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.baidu.mobstat.StatService
import com.jbangit.pushdemo.api.TestRepo
import com.jbangit.pushdemo.model.AliasAndTags
import com.jbangit.pushdemo.model.NotifyData
import com.jbangit.pushdemo.umeng.UmengPush
import com.jbangit.unimini.JBUniMini
import com.jbangit.unimini.model.Result
import com.jbangit.unimini.showToast
import com.jbangit.unimini.toUniPage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    val push by lazy { UmengPush.init(this) }

    val repo by lazy { TestRepo(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tags = findViewById<EditText>(R.id.tagsText)
        val userText = findViewById<EditText>(R.id.userText)
        val pushWithTags = findViewById<View>(R.id.pushWithTags)

        pushWithTags?.setOnClickListener {
            val tag = tags.text?.toString() ?: ""
            if (tag.isEmpty()) {
                showToast("请输入tags")
                return@setOnClickListener
            }
            pushWithTags(tag)
        }

        userText.addTextChangedListener {
            UniUserModule.userId = it.toString().trim()
        }

        //检查并更新uni
        JBUniMini.upgradeCacheUni(this)


        repo.getAliasAndTags().enqueue(object : Callback<Result<AliasAndTags>> {
            override fun onResponse(
                call: Call<Result<AliasAndTags>>,
                response: Response<Result<AliasAndTags>>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.e("TAG", "onResponse: ${result?.data}")
                    if (result?.code == 0) {
                        result.data?.apply {
                            tags.setText(tag)
                            val tagList = tag?.split(",") ?: listOf()
                            tagList.forEach {
                                Log.e("TAG", "onResponse: $it")
                            }
                            push.setTags(tagList.toTypedArray())
                            push.setAlias(upushAlias ?: "", upushAliasType ?: "")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Result<AliasAndTags>>, t: Throwable) {
            }


        })

        findViewById<View>(R.id.push).setOnClickListener {
            pushWithAlias()
        }

        findViewById<View>(R.id.net).setOnClickListener {
            toUniPage("__UNI__4516E07", MySplashView::class)
        }

        fromNotification(intent)
    }

    fun pushWithAlias() {
        val msg = findViewById<TextView>(R.id.msgText).text.toString().trim()
        val path = findViewById<TextView>(R.id.targetPathText).text.toString().trim()

        repo.push(msg, path).enqueue(object : Callback<Result<Any>> {
            override fun onResponse(call: Call<Result<Any>>, response: Response<Result<Any>>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 0) {
                        showToast("推送成功")
                    } else {
                        Log.e("TAG", "onResponse: ${result?.message}")
                        showToast("推送失败")
                    }
                } else {
                    Log.e("TAG", "onResponse: ${response.message()}")
                    showToast("推送失败")
                }
            }

            override fun onFailure(call: Call<Result<Any>>, t: Throwable) {
                t.printStackTrace()
                showToast("推送失败")
            }

        })
    }

    fun pushWithTags(tags: String) {
        val msg = findViewById<TextView>(R.id.msgText).text.toString().trim()
        val path = findViewById<TextView>(R.id.targetPathText).text.toString().trim()
        repo.push(tags, msg, path).enqueue(object : Callback<Result<Any>> {
            override fun onResponse(call: Call<Result<Any>>, response: Response<Result<Any>>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 0) {
                        showToast("推送成功")
                    } else {
                        Log.e("TAG", "onResponse: ${result?.message}")
                        showToast("推送失败")
                    }
                } else {
                    Log.e("TAG", "onResponse: ${response.message()}")
                    showToast("推送失败")
                }
            }

            override fun onFailure(call: Call<Result<Any>>, t: Throwable) {
                t.printStackTrace()
                showToast("推送失败")
            }

        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        fromNotification(intent)
    }

    fun fromNotification(intent: Intent?) {
        val data = intent?.getParcelableExtra<NotifyData>("notification")
        if (data != null) {
            toUniPage("__UNI__4516E07", redirectPath = data.targetPath)
        }
    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this);
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

}