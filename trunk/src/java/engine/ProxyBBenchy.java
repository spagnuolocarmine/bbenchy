/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.net.URL;
import java.net.HttpURLConnection;
import javax.sql.rowset.spi.XmlReader;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


import org.w3c.tidy.Tidy;

public class ProxyBBenchy extends HttpServlet {

 private ServletContext servletContext;
    private Logger log;
    
    public void init(ServletConfig servletConfig) throws ServletException {
        servletContext = servletConfig.getServletContext();
        log = Logger.getLogger(ProxyBBenchy.class.getName());
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response){

        BufferedInputStream webToProxyBuf = null;
        BufferedOutputStream proxyToClientBuf = null;
        HttpURLConnection con;
        
        try{
            int statusCode;
            int oneByte;
            String methodName;
            String headerText;
            System.out.println("wfuhrifghifhirhi"+ request.getParameter("siteName") );
            String urlString = "http://"+(String)request.getParameter("siteName");
            String queryString = request.getQueryString();
            
            urlString += queryString==null?"":"?"+queryString;
            URL url = new URL(urlString);
            
            log.info("Fetching >"+url.toString());
            
            con =(HttpURLConnection) url.openConnection();
            
            methodName = request.getMethod();
            con.setRequestMethod(methodName);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setFollowRedirects(false);
            con.setUseCaches(true);

            for( Enumeration e = request.getHeaderNames() ; e.hasMoreElements();){
                String headerName = e.nextElement().toString();
                con.setRequestProperty(headerName,    request.getHeader(headerName));
            }

            con.connect();
            
            if(methodName.equals("POST")){
                BufferedInputStream clientToProxyBuf = new BufferedInputStream(request.getInputStream());
                BufferedOutputStream proxyToWebBuf  = new BufferedOutputStream(con.getOutputStream());
                
                while ((oneByte = clientToProxyBuf.read()) != -1) 
                    proxyToWebBuf.write(oneByte);
                
                proxyToWebBuf.flush();
                proxyToWebBuf.close();
                clientToProxyBuf.close();
            }
            
            statusCode = con.getResponseCode();
            response.setStatus(statusCode);
            
            for( Iterator i = con.getHeaderFields().entrySet().iterator() ; i.hasNext() ;){
                Map.Entry mapEntry = (Map.Entry)i.next();
                if(mapEntry.getKey()!=null)
                    response.setHeader(mapEntry.getKey().toString(), ((List)mapEntry.getValue()).get(0).toString());
            }
            
            webToProxyBuf = new BufferedInputStream(con.getInputStream());
            proxyToClientBuf = new BufferedOutputStream(response.getOutputStream());
            
            
            
            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            Document responseBenFormat = tidy.parseDOM(webToProxyBuf,null);
            
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath=factory.newXPath();
            String pattern = "//a[@class='link']/@href";
            NodeList nodes = (NodeList)xPath.evaluate(pattern, response, XPathConstants.NODESET);
            System.out.println("Stampo link");
            for (int i = 0; i < nodes.getLength(); i++) {
                 System.out.println((String) nodes.item(i).getNodeValue());
            }
            
            
            while ((oneByte = webToProxyBuf.read()) != -1) 
                proxyToClientBuf.write(oneByte);

            proxyToClientBuf.flush();
            proxyToClientBuf.close();

            webToProxyBuf.close();
            con.disconnect();
            
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        finally{
        }
    }
}
