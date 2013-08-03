package com.goodow.drive.server.proxy;

import com.google.inject.Singleton;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class CrossDomainFilter implements Filter {

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
      throws IOException, ServletException {
    filterChain.doFilter(req, resp);
    ((HttpServletResponse) resp).setHeader("Access-Control-Allow-Origin", "*");
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }

}
