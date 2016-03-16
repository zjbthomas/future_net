package com.filetool.main;

import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;
import com.routesearch.route.Route;

/**
 * �������
 * 
 * @author
 * @since 2016-3-1
 * @version v1.0
 */
public class Main
{
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.err.println("please input args: graphFilePath, conditionFilePath, resultFilePath");
            return;
        }

        String graphFilePath = args[0];
        String conditionFilePath = args[1];
        String resultFilePath = args[2];

        LogUtil.printLog("Begin");

        // ��ȡ�����ļ�
        String graphContent = FileUtil.read(graphFilePath, null);
        String conditionContent = FileUtil.read(conditionFilePath, null);

        // ����ʵ�����
        String resultStr = Route.searchRoute(graphContent, conditionContent);

        // д������ļ�
        FileUtil.write(resultFilePath, resultStr, false);

        LogUtil.printLog("End");
    }

}
