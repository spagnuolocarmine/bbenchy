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

       
        String urlString = "http://"+(String)request.getParameter("siteName");
    }    
}
