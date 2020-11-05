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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rs.igram.kiribi.io.*;

public class StoreDelegateTest {
   @TempDir Path root;
   String scheme = "a";
   
   @Test
   public void testPreexisting() throws IOException {
   	   assertTrue(Files.isDirectory(root));
   	   
   	   StoreDelegate delegate = new StoreDelegate(root, scheme);  	   
   	   assertFalse(delegate.preexisting());
   	   
   	   delegate = new StoreDelegate(root, scheme);
   	   assertTrue(delegate.preexisting());
   }
	
   @Test
   public void testExists() throws IOException {
   	   StoreDelegate delegate = new StoreDelegate(root, scheme); 
   	   String name = "b";
   	   
   	   assertFalse(delegate.exists(name));
   	   
   	   Path path = delegate.dir.resolve(name);
   	   Files.createFile(path);
   	   
   	   assertTrue(delegate.exists(name));
   }
	
   @Test
   public void testRemove() throws IOException {
   	   StoreDelegate delegate = new StoreDelegate(root, scheme); 
   	   String name = "c";
   	   
   	   assertFalse(delegate.exists(name));
   	   
   	   Path path = delegate.dir.resolve(name);
   	   Files.createFile(path);
   	   
   	   assertTrue(delegate.exists(name));
   	   delegate.remove(name);
   	   assertFalse(delegate.exists(name));
   }
	
   @Test
   public void testIO() throws IOException {
   	   StoreDelegate delegate = new StoreDelegate(root, scheme); 
   	   String name = "d";
   	   Path path = delegate.dir.resolve(name);
   	   
   	   byte[] b = new byte[100];
   	   SecureRandom random = new SecureRandom();
   	   random.nextBytes(b);
   	   
   	   delegate.out(b, path);
   	   VarInputStream in = delegate.in(path);
   	   byte[] test = new byte[100];
   	   in.readFully(test);
   	   assertTrue(Arrays.equals(b, test));
   }
}