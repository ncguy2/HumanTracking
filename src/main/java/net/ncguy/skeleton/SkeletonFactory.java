package net.ncguy.skeleton;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.ik.Bone;
import net.ncguy.api.ik.BoneNode;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

public class SkeletonFactory {

    File file;

    public SkeletonFactory(File file) {
        this.file = file;
    }

    public BoneNode Parse() {
        try {
            FileInputStream stream = new FileInputStream(this.file);
            return Parse(stream);
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }

    Stack<Integer> boneChainLengths = new Stack<>();

    public BoneNode Parse(InputStream is) throws XMLStreamException {
        XMLInputFactory fac = XMLInputFactory.newFactory();
        XMLEventReader reader = fac.createXMLEventReader(is);

        AtomicReference<BoneNode> rootBone = new AtomicReference<>(null);
        AtomicReference<BoneNode> currentBone = new AtomicReference<>(null);

        while(reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if(event.isStartElement()) {
                StartElement element = event.asStartElement();

                QName name = element.getName();
                String localPart = name.getLocalPart();

                BoneNode boneNode = null;
                if(localPart.equalsIgnoreCase("Bone")) {
                    boneNode = BuildBone(element, currentBone.get());
                }else if(localPart.equalsIgnoreCase("BoneChain")) {
                    boneNode = BuildBoneChain(element, currentBone.get());
                }

                if(boneNode != null) {
                    if(rootBone.get() == null)
                        rootBone.set(boneNode);
                    currentBone.set(boneNode);
                }

            }

            if(event.isEndElement()) {
                EndElement element = event.asEndElement();

                QName name = element.getName();
                String localPart = name.getLocalPart();

                if(localPart.equalsIgnoreCase("Bone")) {
                    currentBone.set(currentBone.get().parent);
                }else if(localPart.equalsIgnoreCase("BoneChain")) {
                    currentBone.set(GetBoneChainRoot(element, currentBone.get()));
                }
            }

        }

        return rootBone.get();
    }

    public BoneNode BuildBone(StartElement element, BoneNode parent) {

        String idStr = element.getAttributeByName(QName.valueOf("id")).getValue();
        String directionStr = element.getAttributeByName(QName.valueOf("direction")).getValue();
        String lengthStr = element.getAttributeByName(QName.valueOf("length")).getValue();


        String[] split = directionStr.split(",");
        float[] vals = new float[3];
        for (int i = 0; i < split.length; i++)
            vals[i] = Float.parseFloat(split[i].trim());

        float length = Float.parseFloat(lengthStr);

        Vector3 position = new Vector3();

        if(parent != null)
            position.set(parent.bone.GetPosition()).add(parent.bone.EndPosition());

        Bone bone = new Bone(idStr, position, length);
        bone.GetPosition().set(vals);
        return new BoneNode(bone, parent);
    }

    public BoneNode BuildBoneChain(StartElement element, BoneNode parent) {
        String prefixStr = element.getAttributeByName(QName.valueOf("prefix")).getValue();
        String directionStr = element.getAttributeByName(QName.valueOf("direction")).getValue();
        String lengthStr = element.getAttributeByName(QName.valueOf("length")).getValue();
        String bonesStr = element.getAttributeByName(QName.valueOf("bones")).getValue();

        String[] split = directionStr.split(",");
        float[] vals = new float[3];
        for (int i = 0; i < split.length; i++)
            vals[i] = Float.parseFloat(split[i].trim());
        float length = Float.parseFloat(lengthStr);

        int boneCount = Integer.parseInt(bonesStr);

        boneChainLengths.push(boneCount);

        BoneNode current = parent;

        Vector3 position = new Vector3();

        for (int i = 0; i < boneCount; i++) {

            if(current != null)
                position.set(current.bone.EndPosition());

            Bone bone = new Bone(prefixStr + "_" + i, position, length);
            bone.GetPosition().set(vals);

            current = new BoneNode(bone, current);
        }

        return current;
    }

    public BoneNode GetBoneChainRoot(EndElement element, BoneNode currentBone) {
        Integer boneCount = boneChainLengths.pop();

        BoneNode c = currentBone;

        for (Integer i = 0; i < boneCount; i++)
            c = c.parent;

        return c;
    }

}
