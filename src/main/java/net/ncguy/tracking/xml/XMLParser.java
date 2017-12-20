package net.ncguy.tracking.xml;

import net.ncguy.tracking.geometry.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class XMLParser {

    public static GeometryItem ParseXML(File xmlFile, List<GeometryItem> items) {
        try {
            return ParseXML_Impl(xmlFile, items);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GeometryItem ParseXML_Impl(File xmlFile, List<GeometryItem> items) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlFile);

        doc.getDocumentElement().normalize();

        NodeList field = doc.getElementsByTagName("Field");

        Node item = field.item(0);

        return BuildItem(item, items);
    }

    public static GeometryItem BuildItem(Node node, List<GeometryItem> items) {
        if(node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            GeometryItem item = new GeometryItem();
            items.add(item);

            item.path = element.getAttribute("id");
            if(element.hasAttribute("operation")) {
                String operation = element.getAttribute("operation");
                item.operation = Model.Operation.valueOf(operation);
                NodeList childNodes = element.getChildNodes();
                if(childNodes.getLength() > 0) {
                    Node item1 = GetElement(childNodes, 0);
                    item.left = BuildItem(item1, items);
                    if(childNodes.getLength() > 1) {
                        Node item2 = GetElement(childNodes, 1);
                        item.right = BuildItem(item2, items);
                    }
                }
            }else {
                String primitive = element.getAttribute("primitive");
                GeometryTypes type = GeometryTypes.valueOf(primitive);

                float ax = GetFloat(element, "ax", 0.f);
                float ay = GetFloat(element, "ay", 0.f);
                float az = GetFloat(element, "az", 0.f);
                float aw = GetFloat(element, "aw", 0.f);

                float bx = GetFloat(element, "bx", 0.f);
                float by = GetFloat(element, "by", 0.f);
                float bz = GetFloat(element, "bz", 0.f);
                float bw = GetFloat(element, "bw", 0.f);

                float c = GetFloat(element, "c", 0.f);

                float roll  = GetFloat(element, "roll", 0.f);
                float pitch = GetFloat(element, "pitch", 0.f);
                float yaw   = GetFloat(element, "yaw", 0.f);

                DefaultGeometry geom = new DefaultGeometry(type, GetVec(ax, ay, az, aw), GetVec(bx, by, bz, bw), c);
                Model model = new Model(geom, Model.Operation.UNION);
                model.rotation.setEulerAngles(yaw, pitch, roll);
                item.operation = Model.Operation.UNION;
                item.data = model;
            }

            return item;
        }
        return null;
    }

    public static Element GetElement(NodeList list, int index) {
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            Node item = list.item(i);
            if(item.getNodeType() == Node.ELEMENT_NODE) {
                if(index > 0)
                    index--;
                else
                    return (Element) item;
            }
        }
        return null;
    }

    public static BaseGeometry.Vec4 GetVec(float x, float y, float z, float w) {
        return new BaseGeometry.Vec4(x, y, z, w);
    }

    public static float GetFloat(Element node, String attr, float def) {
        String attribute = node.getAttribute(attr);
        if(attribute.isEmpty()) return def;
        try {
            return Float.parseFloat(attribute);
        }catch(NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return def;
    }

}
