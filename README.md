## 接入uni

1,将uniLib，unimini拷贝到项目根目录里。

并在settings.gradle文件末尾加上：

```groovy

include ':unimini'
include ':uniLib:and-gif'
include ':uniLib:oaid-sdk'
include ':uniLib:uni-mpsdk-v2'
include ':uniLib:uniapp-v8'

```

2，修改app下的build.gradle文件

```groovy
android {

    defaultConfig {
        //code...
        ndk {
            abiFilters 'x86', 'armeabi-v7a', "arm64-v8a" //不支持armeabi
        }

        manifestPlaceholders = ["apk.applicationId": "com.edifier"]
        //code...
    }

    //code...

    aaptOptions {
        additionalParameters '--auto-add-overlay'
        ignoreAssetsPattern "!.svn:!.git:.*:!CVS:!thumbs.db:!picasa.ini:!*.scc:*~"
    }

}

dependencies {
    api project(":unimini")
}
```

3,初始化UniMini，建议在application中初始化。

```kotlin

class App : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        JBUniMini.init(this){builder ->
            //builder 是uni的配置
            //this 是JBUimini
            loadingDialog = null //配置更新uni的进度对话框
            uniCacheDir = "" //配置uni缓存目录，在外部私有目录cache下
            uniPush = null//配置推送扩展
            uniStatistics = null//配置应用统计扩展
        }
    }
}
```

4，JBUniMini的Api
```kotlin
    JBUniMini.registerModule(kClass) //注册uni扩展原生的模块

/**
 * 打开uni界面,如果[appIdOrUrl]为在线的uni下载链接，那么会先检查当前app中是否包含该uni，如果包含，那么直接打开，否则将去下载
 * 而如果[appIdOrUrl]是uni的appId，那么将会去寻找是否存在该内置uni，如果不存在，则会调用api获取该uni的版本，和本地缓存的uni版本进行对比（如果本地不存在缓存的uni相当于本地存在缓存的uni但是版本最低）
 * 当本地缓存的uni的版本低于线上的，那么将会去下载。否则将直接打开本地缓存的uni。当下载完成时，将直接打开
 * @param appIdOrUrl 可以是内置的uni的appId，也可以是在线的uni下载链接
 * @param splashKClass 启动页面，建议设置
 * @param redirectPath uni的路径，如果不设置则打开uni小程序首页
 * @param arguments 传递的参数
 */
    fun toUniPage(
    owner: LifecycleOwner,
    appIdOrUrl: String? = null,
    splashKClass: KClass<out IDCUniMPAppSplashView>? = null,
    redirectPath: String? = null,
    arguments: JSONObject? = null
    )
    /**
     * 更新缓存的uni
     * 会遍历缓存的目录，将需要升级的uni进行升级
     */
    fun upgradeCacheUni(owner: LifecycleOwner)
```

由于在gradle.properties中使用了敏感信息，所以在远程不会存在该文件，需要创建，并加入以下下内容：
```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
```


