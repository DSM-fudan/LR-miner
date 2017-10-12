package cn.edu.fudan.dsm.tslrm.log;

import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-5
 * Time: 下午9:44
 * To change this template use File | Settings | File Templates.
 */
public class DefaultFileAppender extends FileAppender {
    static PatternLayout defaultLayout = new PatternLayout("%d %p [%c %L] - <%m>%n");

    public DefaultFileAppender(String fileName) throws IOException {
        super(defaultLayout,fileName);
    }

    static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyyMMdd HH_mm_ss");

    public DefaultFileAppender(Class clazz) throws IOException {
        this("log" + "//" + clazz.getSimpleName() + "-" + defaultDateFormat.format(new Date()) + ".txt");
    }

}
