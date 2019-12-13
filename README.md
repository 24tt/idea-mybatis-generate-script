# idea-mybatis-generate-script
基于IDEA数据库插件编写的mybatis代码生成脚本

# 安装脚本

下载脚本文件，放在IDEA Database插件脚本目录下。

如果你是Mac系统，请将脚本放置在如下地址：
`{用户目录}/Library/Preferences/IntelliJIdea{版本号}/extensions/com.intellij.database/schema`

# 配置脚本
按需修改脚本中`用户自定义`部分，建议不要轻易修改其他部分。

# 使用脚本
打开`Database`插件，创建数据库连接，右键某数据库表，在弹出的菜单中一次点击`Scripted Extensions`->`GenerateCode.groovy`(脚本名称)

无报错，则执行成功。前往对应的`outDir`(文件输出目录)查看，文件输出目录默认在项目根目录下，以`generate`命名。

若报错，则打开IDEA右下角`Event Log`控制台，查看具体报错信息。当然，如果报错，IDEA右下角也会有浮窗提示。

# Finally
如有问题请留言或私信
