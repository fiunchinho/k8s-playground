package com.schibsted.hello;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloWorldController{

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String sayHello() throws UnknownHostException {
    return "This is hello1 responding to / from " + InetAddress.getLocalHost().getHostName() + "(" + InetAddress.getLocalHost() + ")";
  }

  @RequestMapping(value = "/foo", method = RequestMethod.GET)
  public String foor() throws UnknownHostException {
    return "This is hello1 responding to /foo from " + InetAddress.getLocalHost().getHostName() + "(" + InetAddress.getLocalHost() + ")";
  }

  @RequestMapping(value = "/bar", method = RequestMethod.GET)
  public String bar() throws UnknownHostException {
    return "This is hello1 responding to /bar from " + InetAddress.getLocalHost().getHostName() + "(" + InetAddress.getLocalHost() + ")";
  }
}

