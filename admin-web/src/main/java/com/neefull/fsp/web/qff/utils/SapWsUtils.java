package com.neefull.fsp.web.qff.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.springframework.http.RequestEntity;
import sun.misc.BASE64Encoder;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * sap webservice的工具类，目的在于实现连接sapwebservice
 */
@Slf4j
public abstract class SapWsUtils {


    private static final String encoding = "UTF-8";

    /**
     * 调用sap webservie
     *
     * @param invoke_ws_url 调用地址
     * @param soapMsg       soap消息
     * @return 返回的字符串
     * @throws Exception
     */
    public static StringBuffer callWebService(String invoke_ws_url, String soapMsg) throws Exception {
        // 开启HTTP连接ַ
        InputStreamReader isr = null;
        BufferedReader inReader = null;
        StringBuffer result = null;
        OutputStream outObject = null;
//        String input = userName + ":" + password;
//        BASE64Encoder base = new BASE64Encoder();
//        String encodedPassword = base.encode(input.getBytes(encoding));

        try {
            URL url = new URL(invoke_ws_url);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
//            httpConn.setRequestProperty("Authorization", "Basic " + encodedPassword);

            // 设置HTTP请求相关信息
            httpConn.setRequestProperty("Content-Length", String.valueOf(soapMsg.getBytes().length));
            httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setConnectTimeout(500000);
            httpConn.setReadTimeout(500000);

            // 进行HTTP请求
            outObject = httpConn.getOutputStream();
            outObject.write(soapMsg.getBytes("utf-8"));
//            System.out.println(httpConn.getResponseMessage());
            log.info(httpConn.getResponseMessage());
            // 获取HTTP响应数据

            isr = new InputStreamReader(httpConn.getInputStream(), encoding);
            inReader = new BufferedReader(isr);
            result = new StringBuffer();
            String inputLine;
            while ((inputLine = inReader.readLine()) != null) {
                result.append(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();

//            log.error("连接esp服务器失败，失败原因为:{}",e.getMessage());
        } finally {
            // 关闭输入流
            if (inReader != null) {
                inReader.close();
            }
            if (isr != null) {
                isr.close();
            }
            // 关闭输出流
            if (outObject != null) {
                outObject.close();
            }
        }
        return result;
    }

    /**构建QFF  soap请求报文   固定形式
     * @param fromDate
     * @param toDate
     * @return
     */
    public static String getSoapMessage(String fromDate,String toDate){

        StringBuffer message = new StringBuffer("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://www.shaphar.com/SoapService\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <soap:REQUEST_DATA>\n" +
                "         <soap:commonHeader>\n" +
                "            <soap:BIZTRANSACTIONID>1</soap:BIZTRANSACTIONID>\n" +
                "            <soap:COUNT>1</soap:COUNT>\n" +
                "            <soap:CONSUMER>1</soap:CONSUMER>\n" +
                "            <soap:SRVLEVEL>1</soap:SRVLEVEL>\n" +
                "            <soap:ACCOUNT>1</soap:ACCOUNT>\n" +
                "            <soap:PASSWORD>1</soap:PASSWORD>\n" +
                "            <soap:COMMENTS>1</soap:COMMENTS>\n" +
                "         </soap:commonHeader>\n" +
                "         <soap:LIST><![CDATA[<urn:ZCHN_FM_QFF xmlns:urn=\"urn:sap-com:document:sap:rfc:functions:ZCHN_FM_QFF_BS\"><ET_QFF>\n" +
                "            <item>\n" +
                "               <QMNUM></QMNUM>\n" +
                "               <HERKUNFT></HERKUNFT>\n" +
                "               <MAWERK></MAWERK>\n" +
                "               <MATNR></MATNR>\n" +
                "               <MSTAE></MSTAE>\n" +
                "               <CHARG></CHARG>\n" +
                "               <IDNLF></IDNLF>\n" +
                "               <BISMT></BISMT>\n" +
                "               <LICHN></LICHN>\n" +
                "               <HSDAT></HSDAT>\n" +
                "               <VFDAT></VFDAT>\n" +
                "               <MGEIG></MGEIG>\n" +
                "               <QMTXT></QMTXT>\n" +
                "               <ZPROCLAS></ZPROCLAS>\n" +
                "               <REGNO></REGNO>\n" +
                "               <AWBNO></AWBNO>\n" +
                "               <ERDAT></ERDAT>\n" +
                "            </item>\n" +
                "         </ET_QFF>\n" +
                "         <ET_QFF_ATT>\n" +
                "            <item>\n" +
                "               <QMNUM></QMNUM>\n" +
                "               <ATTACHNAME></ATTACHNAME>\n" +
                "               <ATTACH></ATTACH>\n" +
                "            </item>\n" +
                "         </ET_QFF_ATT>\n" +
                "         <IV_DATE_FROM>"+fromDate+"</IV_DATE_FROM>\n" +
                "         <IV_DATE_TO>"+toDate+"</IV_DATE_TO> \n" +
                "       </urn:ZCHN_FM_QFF>]]></soap:LIST>\n" +
                "      </soap:REQUEST_DATA>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>");

        return message.toString();
    }



    /**
     * 根据消息模板组装消息
     *
     * @param methodName 方法名称
     * @return
     */
    public String getSoapMsg(String methodName, Map<String, String> params) {
        String fileName = this.getClass().getResource("/soap/" + methodName + ".xml").getPath();
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        FileInputStream in = null;
        String sendMsg = null;
        try {
            in = new FileInputStream(file);
            in.read(filecontent);
            sendMsg = new String(filecontent, encoding);
            //解析出来占位符信息，并按照map里面参数进行匹配设置
            if (params.size() > 0) {
                for (String key : params.keySet()) {
                    sendMsg = sendMsg.replace("${" + key + "}", params.get(key));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sendMsg;
    }

}
