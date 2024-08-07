package com.reine.utils;

import java.util.Random;

/**
 * @author Iammm 2024/8/7 16:41
 */
public class RandomIndexUtils {

  public static int getRandomIndex(int arrayLength) {
    Random random = new Random();
    return random.nextInt(arrayLength);
  }

}
