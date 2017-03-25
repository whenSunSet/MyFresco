# MyFresco
> 这是基于Fresco 0.12版整合和翻译的Fresco源码，目前获取网络图片使用是HttpConnection。Fresco支持将HttpConnection更换为Volley或者OkHttp。
目前这一部分代码还没有整合进去，但是并不影响项目的运行。由于是第一次翻译所以翻译之中肯定是有错误的，大家凑合着看，我也会持续修正翻译。接下来我会写一系列
博客，用于介绍和分析Fresco源码中的各个模块的设计。如果有问题可以加我QQ1018998632(备注：fresco源码学习)，我的博客在简书上，我在简书中的名字是**何时夕**。
## 一、概述
- 1.本项目基于gradle2.10和AndroidStudio2.1.2，在导入的时候，大家要确认自己的AndroidStudio已经可以使用NDK开发。
- 2.com.facebook.fresco.samples.showcase：这个下面是Fresco官方源码的完整demo，几乎所有Fresco能做到的功能都有，目前还没有翻译，大家如果想直接学习怎么使用Fresco，可以直接看这里面的代码
- 3.除了1中说的包，其他的包都是Fresco的源代码，在之后的博客里我会一一讲解。
- 4.jniLibs：这里是Fresco中使用到的c++代码，主要用于Webp、gif、Jpeg等图片的解码编码，以及一些图形变换操作。

## 二、Fresco的硬盘缓存模块
[Fresco的硬盘缓存模块博客地址](http://www.jianshu.com/p/ab2124764438)
