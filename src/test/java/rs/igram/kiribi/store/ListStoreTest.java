/* 
 * MIT License
 * 
 * Copyright (c) 2020 Igram, d.o.o.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
package rs.igram.kiribi.store;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rs.igram.kiribi.io.*;

public class ListStoreTest {	
	@TempDir Path root;
    Decoder<Bar> decoder = Bar::new;
    Function<Bar, String> namer = Bar::s;
    
   @Test
   public void testList() throws IOException {
   	   String scheme = "a";
   	   StoreDelegate delegate = new StoreDelegate(root, scheme); 
   	   ListStore store = new ListStore(Bar[].class, decoder, namer, delegate);  
   	   store.list();
   	   Bar bar1 = new Bar(1, "A");
   	   Bar bar2 = new Bar(2, "B");
   	   
   	   store.add(bar1, bar2);
   	   
   	   List<Bar> list = store.list();
   	   assertEquals(list.size(), 2);
   	   assertTrue(list.contains(bar1));
   	   assertTrue(list.contains(bar2));
   }

   @Test
   public void testName() throws IOException {
   	   Bar bar1 = new Bar(1, "A");
   	   Bar bar2 = new Bar(2, "B");
   	   assertEquals(namer.apply(bar1), "A");
   	   assertNotEquals(namer.apply(bar2), "A");
   }
	
   @Test
   public void testReplace() throws IOException {
   	   String scheme = "b";
   	   StoreDelegate delegate = new StoreDelegate(root, scheme); 
   	   ListStore store = new ListStore(Bar[].class, decoder, namer, delegate);  
   	   store.list();
   	   Bar bar1 = new Bar(1, "A");
   	   Bar bar2 = new Bar(2, "A");
   	   
   	   store.add(bar1);
   	   
   	   Bar test = (Bar)store.get("A");
   	   assertNotNull(test);
   	   assertEquals(test, bar1);
   	   
   	   store.replace(bar2);
   	   
   	   test = (Bar)store.get("A");
   	   assertNotNull(test);
   	   assertEquals(test, bar2);
   	   assertNotEquals(bar1, bar2);
   	   assertNotEquals(test, bar1);
   	   
   	   List<Bar> list = store.list();
   	   assertEquals(list.size(), 1);
   	   assertFalse(list.contains(bar1));
   	   assertTrue(list.contains(bar2));
   }
	
   @Test
   public void testRemove() throws IOException {
   	   String scheme = "c";
   	   StoreDelegate delegate = new StoreDelegate(root, scheme); 
   	   ListStore store = new ListStore(Bar[].class, decoder, namer, delegate);  
   	   List<Bar> list = store.list();
   	   assertEquals(list.size(), 0);
   	   
   	   Bar bar = new Bar(1, "A");
   	   
   	   store.add(bar);
   	   list = store.list();
   	   assertEquals(list.size(), 1);
   	   
   	   Bar test = (Bar)store.get("A");
   	   assertNotNull(test);
   	   assertEquals(test, bar);
   	   
   	   store.remove(bar);
   	   test = (Bar)store.get("A");
   	   assertNull(test);
   	   
   	   list = store.list();
   	   assertEquals(list.size(), 0);
   }
	
   @Test
   public void testAdd() throws IOException {
   	   String scheme = "d";
   	   StoreDelegate delegate = new StoreDelegate(root, scheme); 
   	   ListStore store = new ListStore(Bar[].class, decoder, namer, delegate);  
   	   store.list();
   	   Bar bar = new Bar(1, "A");
   	   
   	   store.add(bar);
   	   
   	   Bar test = (Bar)store.get("A");
   	   assertNotNull(test);
   	   
   	   List<Bar> list = store.list();
   	   assertEquals(list.size(), 1);
   	   assertTrue(list.contains(bar));
   }
}