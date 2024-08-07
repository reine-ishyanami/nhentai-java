package com.reine.entity;

import java.util.List;

/**
 * @author Iammm 2024/8/7 11:33
 */
public record Preview(String name, List<String> labels, String gallery, String cover,
                      List<String> language) {

}
