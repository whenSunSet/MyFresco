package com.facebook.binaryresource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
/*
* 这个接口代表一个字节的序列，它抽象的底层的资源，比如一个 file文件
*
* 它代表一个 non-volatile 的资源，它可以被多次读取。
* 因为它是基于流的,执行转换加密可以通过*简单的包装,而不是原始文件的解密内容写到一个新文件。
*
* 它受Guava的ByteSource启发，但是它没有使用Guava的实现
* */
public interface BinaryResource {

    /**
     * 打开一个InputStream通过从这个资源里读取。这个方法在每次调用的时候应该返回体格独立的流
     * 调用者应该保证返回的流是关闭的
     */
    InputStream openStream() throws IOException;


    /**
     * 读取所有资源到一个byte数组中从这个资源里
     */
    byte[] read() throws IOException;

    /**
     * 返回这个资源一个有多少byte数，这可能是一个重量级的操作当打开一个流的时候，所以如果可能
     * 在流关闭的时候返回byte数，而不是为了返回一个byte数，而打开一个流。
     */
    long size();
}
