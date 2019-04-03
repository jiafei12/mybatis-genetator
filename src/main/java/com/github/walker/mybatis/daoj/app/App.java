package com.github.walker.mybatis.daoj.app;

import com.github.walker.mybatis.daoj.core.Generator;

import java.io.*;

/**
 * @author amber
 */
public class App
{
    public static void main(String[] args) throws IOException
    {
        try {
            new Generator().generator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
