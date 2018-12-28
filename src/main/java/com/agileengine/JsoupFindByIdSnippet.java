package com.agileengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class JsoupFindByIdSnippet {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupFindByIdSnippet.class);

    private static String CHARSET_NAME = "utf8";

    private static final String TARGET_ELEMENT_ID_DEFAULT = "make-everything-ok-button";

    public static void main(String[] args) {

        if (args.length < 2) {
            LOGGER.error("Invalid arguments amount: {}", args.length);
            return;
        }

        String originFilePath = args[0];
        String sampleFilePath = args[1];

        String targetElementId = args.length > 2 ? args[2] : TARGET_ELEMENT_ID_DEFAULT;

        ElementOrigin elementOrigin = getAllInfoByOriginElement(new File(originFilePath), targetElementId);
        List<Element> sampleFileElements = findElementsByTagName(new File(sampleFilePath), elementOrigin.getElement().tagName());

        Element suitableElement = findSuitableElement(elementOrigin, sampleFileElements);

        String absolutePath = getAbsolutePath(suitableElement);

        LOGGER.info("Absolute path: [{}]", absolutePath);
    }

    private static Element findSuitableElement(ElementOrigin elementOrigin, List<Element> sampleFileElements) {

        Map<Element, Integer> matchingAmounts = new HashMap<>();

        for (Element element : sampleFileElements) {
            Integer matchingAmount = compareElements(elementOrigin, element);
            if (matchingAmount == 0) {
                continue;
            }
            matchingAmounts.put(element, matchingAmount);
        }
        return matchingAmounts.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }

    private static Integer compareElements(ElementOrigin elementOrigin, Element element) {
        Integer matchingAmount = 0;

        matchingAmount = compareText(matchingAmount, elementOrigin.getElement().text(), element.text());
        return compareAttributes(matchingAmount, elementOrigin.getAttributes(), getAttributesMap(element));
    }

    private static Integer compareAttributes(Integer matchingAmount, Map<String, String> originAttributes, Map<String, String> attributesMap) {
        for (Map.Entry<String, String> attr : originAttributes.entrySet()) {
            if (attributesMap.containsKey(attr.getKey())
                    && attributesMap.get(attr.getKey()).equalsIgnoreCase(attr.getValue())) {
                matchingAmount++;
            }
        }
        return matchingAmount;
    }

    private static Integer compareText(Integer matchingAmount, String textOrigin, String text) {
        if ((textOrigin.isEmpty() && text.isEmpty()) ||
                (textOrigin.trim().equalsIgnoreCase(text.trim()))) {
            matchingAmount++;
        }
        return matchingAmount;
        //TODO check by words
    }

    private static List<Element> findElementsByTagName(File htmlFile, String tagName) {
        return getDocument(htmlFile).getElementsByTag(tagName);
    }

    private static ElementOrigin getAllInfoByOriginElement(File htmlFile, String targetElementId) {
        Element elementOrigin = findElementById(htmlFile, targetElementId);

        return new ElementOrigin(elementOrigin, getAttributesMap(elementOrigin));
    }

    private static Map<String, String> getAttributesMap(Element element) {
        return element.attributes().asList().stream()
                .collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));
    }

    private static Element findElementById(File htmlFile, String targetElementId) {
        try {
            return getDocument(htmlFile).getElementById(targetElementId);
        } catch (Exception e) {
            LOGGER.error("Error parsing document [{}] ", targetElementId);
            return null;
        }
    }

    private static Document getDocument(File htmlFile) {
        try {
            return Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return null;
        }
    }
    private static String getAbsolutePath(Element element) {
        List<String> list2 = new ArrayList<>();
        element.parents().forEach(parent -> {
            int siblingIndex = parent.elementSiblingIndex();
            if (siblingIndex > 0) {
                list2.add(0, parent.tagName() + "[" + siblingIndex + "]");
            } else {
                list2.add(0, parent.tagName());
            }
        });

        return list2.stream().collect(Collectors.joining(" > "));
    }
}

class ElementOrigin {

    private Element element;
    Map<String, String> attributes;

    public ElementOrigin(Element element, Map<String, String> attributes) {
        this.element = element;
        this.attributes = attributes;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}