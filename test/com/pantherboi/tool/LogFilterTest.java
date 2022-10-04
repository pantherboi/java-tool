package com.pantherboi.tool;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.pantherboi.tool.LogFilter.Filter;

public class LogFilterTest {

    public static void main(String[] args) throws Exception {
        LogFilter filter = new LogFilter()
        .logLineStartWith("2022/10/03")
        .timestampFormat(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.US))
        .src(new File(".\\test-data\\xxx-2022-10-03.log"))
//        .src(new File(".\\test-data\\tt.txt"))
        .srcEncoding("UTF-8")
        .outputStream(System.out)
//         .skipLineCnt(40)
//        .maxReadLineCnt(500)
//        .addIncludeFilters(new Filter(Filter.TYPE_LINE_CONTAIN,"authenCompany.ok"))
        .addIncludeFilters(new Filter(Filter.TYPE_STACKTRACE_CONTAIN,"deadlock"))
//        .addIncludeFilters(new Filter(Filter.TYPE_STACKTRACE_CONTAIN,"hibernate3"))
//        .addIncludeFilters(new Filter(Filter.TYPE_STACKTRACE_CONTAIN,"branch from upload file"))
//        .addIncludeFilters(new Filter(Filter.TYPE_STACKTRACE_CONTAIN,"epmRest"))
//        .addIncludeFilters(new Filter(Filter.TYPE_LINE_PATTERN,".*authenCompany.ok.*"))
        .printFirstLogLineOnly(true)
        .init();
        filter.read();
        System.out.println("done");
    }

}
