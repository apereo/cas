/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package sample.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple class that provides utilities to ease command line parsing.
 *
 * 
 */
public class SimpleCommandLineParser {

  private Map<String,String> argMap;

  /**
   * Initializes the command line parser by parsing the command line
   * args using simple rules.
   * <p>
   * The arguments are parsed into keys and values and are saved into
   * a HashMap.  Any argument that begins with a '--' or '-' is assumed
   * to be a key.  If the following argument doesn't have a '--'/'-' it
   * is assumed to be a value of the preceding argument.
   */
  public SimpleCommandLineParser(String[] arg) {
    argMap = new HashMap<String,String>();
    for (int i = 0; i < arg.length ; i++) {
      String key;
      if (arg[i].startsWith("--")) {
        key = arg[i].substring(2);
      } else if(arg[i].startsWith("-")) {
        key = arg[i].substring(1);
      } else {
        argMap.put(arg[i], null);
        continue;
      }
      String value;
      int index = key.indexOf('=');
      if (index == -1) {
        if (((i+1) < arg.length) &&
            (arg[i+1].charAt(0) != '-')) {
          argMap.put(key, arg[i+1]);
          i++;
        } else {
          argMap.put(key, null);
        }
      } else {
        value = key.substring(index+1);
        key = key.substring(0, index);
        argMap.put(key, value);
      }
    }
  }

  /**
   * Returns the value of the first key found in the map.
   */
  public String getValue(String ... keys) {
    for(int key_i = 0; key_i < keys.length; key_i++) {
      if(argMap.get(keys[key_i]) != null) {
        return argMap.get(keys[key_i]);
      }
    }
    return null;
  }

  /**
   * Returns true if any of the given keys are present in the map.
   */
  public boolean containsKey(String ... keys) {
    Set<String> keySet = argMap.keySet();
    for (Iterator<String> keysIter = keySet.iterator(); keysIter.hasNext();) {
      String key = keysIter.next();
      for (int key_i = 0; key_i < keys.length; key_i++) {
        if (key.equals(keys[key_i])) {
          return true;
        }
      }
    }
    return false;
  }
}
