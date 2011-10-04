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

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.util.common.xml.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


/**
 * A set of common utility functions used by the samples.
 *
 * 
 */
public class CommonUtils {

  // Make class uninstantiable
  private CommonUtils() {
  }

  /**
   * Utility function to dump the entry as XML to the provided stream.
   */
  public static void dump(BaseEntry entry, OutputStream out)
      throws IOException {
    Writer w = new OutputStreamWriter(out);
    XmlWriter xmlW = new XmlWriter(w, false);
    entry.generateAtom(xmlW, new ExtensionProfile());
    w.flush();
  }

  /**
   * Utility function to dump the feed as XML to the provided stream.
   */
  public static void dump(BaseFeed feed, OutputStream out)
      throws IOException {
    Writer w = new OutputStreamWriter(out);
    XmlWriter xmlW = new XmlWriter(w, false);
    feed.generateAtom(xmlW, new ExtensionProfile());
    w.flush();
  }
}
