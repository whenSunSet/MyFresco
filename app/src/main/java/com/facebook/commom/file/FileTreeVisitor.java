package com.facebook.commom.file;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import java.io.File;

/**
 * 一个实现了这个接口的实例，一定要通过FileTree.walkFileTree方法为了执行一些逻辑操作当遍历文件后代的时候
 * java7提供了一个FileVisitor接口并且Files.walkFileTree方法做了同样的事情，你有更多的选择
 */
public interface FileTreeVisitor {

    /**
     * 在遍历一个目录前调用(包括迭代的根目录)
     */
    void preVisitDirectory(File directory);

    /**
     * 在每个文件被遍历之前调用
     */
    void visitFile(File file);

    /**
     * 在遍历一个目录之后调用(包括迭代的根目录)
     */
    void postVisitDirectory(File directory);
}
