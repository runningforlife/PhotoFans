package com.github.runningforlife.photosniffer.crawler.processor;

/**
 * source to retrieveImages images
 */

public interface ImageSource {

    String URL_YOUWU = "http://www.youwu.cc";
    String URL_MM = "http://www.mmjpg.com";

    String PIXELS_IMAGE_START = "https://images.pexels.com/photos/";
    String POLA_IMAGE_START = "http://ppe.oss-cn-shenzhen.aliyuncs.com/collections";
    String POLA_IMAGE_END = "thumb.jpg";
    String POLA_IMAGE_HIGH_RES = "full_res.jpg";
    int POLA_IMAGE_NUMBER_PER_COLLECTION = 10;
}
