package com.nittodigital.attendancebackend.logic;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.springframework.stereotype.Component;

@Component
public class AxisCreateProductionClient {
    private OMFactory fac;
    private OMNamespace omNs;
    private ServiceClient sc;
    public AxisCreateProductionClient() throws AxisFault {
        fac = OMAbstractFactory.getOMFactory();
        omNs = fac.createOMNamespace("https://ofbiz.apache.org/service/", "ns1");
        sc = new ServiceClient();
        Options opts = new Options();
        opts.setTo(new EndpointReference(
                "https://localhost:8443/webtools/control/SOAPService"));
        opts.setAction("createAttendance");
        sc.setOptions(opts);
    }

    public void sendForInsert(String cardNo, String machineNo, String timestamp)throws AxisFault {

        OMElement res = sc.sendReceive(createPayLoad(cardNo, machineNo, timestamp));
        res.discard();
    }

    private OMElement createPayLoad(String cardNo, String machineNo, String timestamp){
        OMElement findPartiesById = fac.createOMElement("createAttendance", omNs);
        OMElement mapMap = fac.createOMElement("map-Map", omNs);

        findPartiesById.addChild(mapMap);

        mapMap.addChild(createMapEntry("cardId", cardNo));
        mapMap.addChild(createMapEntry("timestamp", timestamp));

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
}
