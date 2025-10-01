package com.ss.emailSendPdf.model;

public class EmailRequest {
private TestObject request;
// action: "send" or "pdf"
private String action;


public TestObject getRequest() { return request; }
public void setRequest(TestObject request) { this.request = request; }
public String getAction() { return action; }
public void setAction(String action) { this.action = action; }
}