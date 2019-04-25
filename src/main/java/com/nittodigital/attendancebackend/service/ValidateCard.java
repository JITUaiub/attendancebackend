package com.nittodigital.attendancebackend.service;

import org.apache.axiom.om.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

@Component
public class ValidateCard {
    private OMFactory fac;
    private OMNamespace omNs;
    private ServiceClient sc;

    private static DocumentBuilderFactory documentBuilderFactory;
    private static DocumentBuilder documentBuilder;
    private static Document document;
    private static NodeList nodeList;

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }

    List<String> dataList = new ArrayList<String>();

        public ValidateCard() throws AxisFault, ParserConfigurationException {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            fac = OMAbstractFactory.getOMFactory();
            omNs = fac.createOMNamespace("https://ofbiz.apache.org/service/", "ns1");
            sc = new ServiceClient();
            Options opts = new Options();
            opts.setTo(new EndpointReference(
                    "https://localhost:8443/webtools/control/SOAPService"));
            opts.setAction("getPersonCardList");
            sc.setOptions(opts);
        }

        public void getAuthValue() throws IOException, ParserConfigurationException, SAXException {

            OMElement res = sc.sendReceive(createPayLoad());
            extractData(res.toString());

        }

        private OMElement createPayLoad(){
            OMElement findPartiesById = fac.createOMElement("getPersonCardList", omNs);
            OMElement mapMap = fac.createOMElement("map-Map", omNs);

            findPartiesById.addChild(mapMap);

            mapMap.addChild(createMapEntry("login.username", "admin"));
            mapMap.addChild(createMapEntry("login.password", "ofbiz"));

            return findPartiesById;
        }

        private OMElement createMapEntry(String key, String val) {

            OMElement mapEntry = fac.createOMElement("map-Entry", omNs);

            // create the key
            OMElement mapKey = fac.createOMElement("map-Key", omNs);
            OMElement keyElement = fac.createOMElement("std-String", omNs);
            OMAttribute keyAttribute = fac.createOMAttribute("value", null, key);

            mapKey.addChild(keyElement);
            keyElement.addAttribute(keyAttribute);

            // create the value
            OMElement mapValue = fac.createOMElement("map-Value", omNs);
            OMElement valElement = fac.createOMElement("std-String", omNs);
            OMAttribute valAttribute = fac.createOMAttribute("value", null, val);

            mapValue.addChild(valElement);
            valElement.addAttribute(valAttribute);

            // attach to map-Entry
            mapEntry.addChild(mapKey);
            mapEntry.addChild(mapValue);

            return mapEntry;
        }

    private void extractData(String xmlInput) throws SAXException, IOException, ParserConfigurationException {
        try {
            dataList.clear();
            document = documentBuilder.parse(new InputSource(
                    new ByteArrayInputStream(xmlInput.getBytes())));
            nodeList = document.getElementsByTagName("eeval-Person");

            for (int x = 0;x < nodeList.getLength(); x++) {
                try {
                    dataList.add(nodeList.item(x).getAttributes().getNamedItem("cardId").getNodeValue().replaceAll(" ", ""));
                }catch (NullPointerException ex){
                    //ex.printStackTrace();
                    dataList.add("Index :" + x);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /*for(int i=0; i<dataList.size(); i++){
            System.out.println("CardNo: " + cardNo + " dataList " + i + " " + dataList.get(i));
            if(dataList.get(i).equals("1234567890")){
                return "3";
            }
        }*/
    }
    }

