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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

import rs.igram.kiribi.io.VarInputStream;

/**
 * An instance of this class manages various aspects of persistent storage.
 *
 * @author Michael Sargent
 */
public class StoreDelegate {
	
	/**
	 * Flag indicating whether the directory associated with this <code>StoreDelegate</code>
	 * existed prior to instatiation.
	 */
	protected final boolean preexisting;
	
	/**
	 * The schema of this store delegate.
	 */
	protected final String[] schema;
	
	/**
	 * The root directory of this store delegate.
	 */
	protected final Path root;
	
	/**
	 * The working directory of this store delegate.
	 */
	protected final Path dir;
	
	/**
	 * Initializes a newly created <code>Address</code> object
	 * with the given parameters.
	 *
	 * <p> <b>Note:</b> The schema are used to construct a path from the root directory to the working directory.</p>
	 * 
	 * @param root TThe root directory of this store delegate.
	 * @param schema The schema of this store delegate.
	 * @throws IOException if there was a problem instantiating an instance of this class.
	 */
	public StoreDelegate(Path root, String... schema) throws IOException {
		this.root = root;
		this.schema = schema;
		
		Path d = root;
		for(String s : schema) d = root.resolve(s);
		dir = d;
		preexisting = Files.exists(dir);
		if(!preexisting) Files.createDirectories(dir);
	}
	
	/**
	 * Returns the schema of this store delegate.
	 *
	 * @return Returns the schema of this store delegate.
	 */	
	public final String[] schema() {return schema;}
	
	/**
	 * Returns <code>true</code> if directory associated with this <code>StoreDelegate</code>
	 * existed prior to instatiation, <code>false</code> otherwise.
	 *
	 * @return Returns <code>true</code> if directory associated with this <code>StoreDelegate</code>
	 * existed prior to instatiation, <code>false</code> otherwise.
	 */	
	public boolean preexisting() {return preexisting;}
	
	/**
	 * Returns <code>true</code> if the file with the given name relative to the associated directory exists, 
	 * <code>false</code> otherwise.  
	 *
	 * @param name The name of the file to test for existence exists.
	 * @return Returns <code>true</code> if the name file relative to the associated directory exists, 
	 * <code>false</code> otherwise. 
	 */	
	public boolean exists(String name) {
		return Files.exists(dir.resolve(name));
	}
		
	/**
	 * Deletes the file with the given name.
	 *
	 * @param name The name of the file to delete.
	 * @throws IOException if there was a problem deleting the file with the given name.
	 */	
	public void remove(String name) throws IOException {
		Files.deleteIfExists(dir.resolve(name));
	}
	
	/**
	 * Returns a <code>VarInputStream</code> initialized with the bytes contained in the 
	 * file determined by the given path.
	 *
	 * @param path The path containing the bytes which will be used to initialize the <code>VarInputStream</code>.
	 * @return Returns a <code>VarInputStream</code> initialized with the bytes contained in the 
	 * file determined by the given path.
	 * @throws IOException if there was a problem reading the file with the given path.
	 */	
	protected VarInputStream in(Path path) throws IOException {
		byte[] b = Files.readAllBytes(path);
        return new VarInputStream(b);
    }
    
    /**
	 * Writes the given byte array to the file determined by the given path.
	 *
	 * @param b The byte array to write.
	 * @param path The path to write to.
	 * @throws IOException if there was a problem writing the given byte array to the file determined by the given path.
	 */	
	protected void out(byte[] b, Path path) throws IOException {
		try(OutputStream out = Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING, WRITE)){
        	out.write(b);
        }
	}
}