package com.facebook.commom.file;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import java.io.File;

/**
 * 一个可以访问 文件树的实用class
 * 在java7中有相似的功能Files.walkFileTree
 * 其方法可以合并入FileUtil(但是似乎其中有一些过于冗长的方法，也许是为了测试，但是无论如何都很疯狂)
 * Utility class to visit a file tree.
 * There's similar functionality in Java 7's Files.walkFileTree method.
 * Its methods could be merge into FileUtil (although it seems to have a lot of
 * crazy redundant methods, maybe for testing, but crazy anyway).
 */
public class FileTree {

    /**
     * 遍历文件树中所有文件，该方法接收一个visitor并且将会在这个文件夹中所有的文件都调用这个方法
     * Iterates over the file tree of a directory. It receives a visitor and will call its methods
     * for each file in the directory.
     * preVisitDirectory (directory)
     * visitFile (file)
     * 为每个目录进行递归
     * - recursively the same for every subdirectory
     * postVisitDirectory (directory)
     * @param directory the directory to iterate
     * @param visitor the visitor that will be invoked for each directory/file in the tree
     */
    public static void walkFileTree(File directory, FileTreeVisitor visitor) {
        visitor.preVisitDirectory(directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.isDirectory()) {
                    walkFileTree(file, visitor);
                } else {
                    visitor.visitFile(file);
                }
            }
        }
        visitor.postVisitDirectory(directory);
    }

    /**
     * 删除这个文件目录中的所有文件包括子目录
     * Deletes all files and subdirectories in directory (doesn't delete the directory
     * passed as parameter).
     */
    public static boolean deleteContents(File directory) {
        File[] files = directory.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                success &= deleteRecursively(file);
            }
        }
        return success;
    }

    /**
     *
     * Deletes the file and if it's a directory deletes also any content in it
     * @param file a file or directory
     * @return true if the file/directory could be deleted
     */
    public static boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            deleteContents(file);
        }
        // if I can delete directory then I know everything was deleted
        return file.delete();
    }

}
