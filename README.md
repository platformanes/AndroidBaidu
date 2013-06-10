AndroidBaidu
============

百度android平台ANE
* 此ANE可提供aser直接用，无需涉及java端。
* 由于百度公开所有SDK 所以所有SDK的内容都有

## 资源

* 官方网站 [baidu](http://app.baidu.com/)
* [开放下载地址](http://developer.baidu.com/wiki/index.php?title=%E5%B8%AE%E5%8A%A9%E6%96%87%E6%A1%A3%E9%A6%96%E9%A1%B5/%E8%B5%84%E6%BA%90%E4%B8%8B%E8%BD%BD)

## 编译方法
* A复制 SDK的JAR到`build/makeJar` ，运行`combine.bat `合并jar
* B 复制A中得到的jar到`build/buildANE/Android-ARM`，运行`ane_packer.bat`得到ANE


## 作者

* [platformANEs](https://github.com/platformanes)由 [zrong](http://zengrong.net) 和 [rect](http://www.shadowkong.com/) 共同发起并完成。