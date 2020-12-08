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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import rs.igram.kiribi.io.Decoder;
import rs.igram.kiribi.io.Encodable;
import rs.igram.kiribi.io.VarInputStream;

import static java.nio.file.StandardCopyOption.*;

/**
 * An instance of this class manages persistent storage of <code>Encodeable</code> objects.
 *
 * @author Michael Sargent
 */
public class ObjectStore<E extends Encodable> {
	protected final Lock lock = new ReentrantLock();
	
	/**
	 * The <code>StoreDelege</code> for this object store.
	 */
	protected final StoreDelegate delegate;
	
	/**
	 * The <code>Decoder</code> for elements of this object store.
	 */
	protected final Decoder<E> decoder;
	
	/**
	 * Initializes a newly created <code>ObjectStore</code> object.
	 *
	 * @param delegate The <code>StoreDelege</code> for this list store.
	 */
	public ObjectStore(StoreDelegate delegate) {
		this(delegate, null);
	}
	
	/**
	 * Initializes a newly created <code>ObjectStore</code> object.
	 *
	 * @param delegate The <code>StoreDelege</code> for this list store.
	 * @param decoder The <code>Decoder</code> for elements of this object store.
	 */
	public ObjectStore(StoreDelegate delegate, Decoder<E> decoder) {
		this.delegate = delegate;
		this.decoder = decoder;
	}
	
	/**
	 * Returns the value of <code>delegate.preexisting()</code>.
	 *
	 * @return Returns the value of <code>delegate.preexisting()</code>. 
	 */	
	public boolean preexisting() {
		lock.lock();
		try{
			return delegate.preexisting;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Returns the value of <code>delegate.exists(String name)</code>.
	 *
	 * @param name The name of the object to test if it exists in this object store.
	 * @return Returns the value of <code>delegate.exists(String name)</code>. 
	 */	
	public boolean exists(String name) {
		lock.lock();
		try{
			return delegate.exists(name);
		} finally {
			lock.unlock();
		}
	}
		
	/**
	 * Removes an object from this <code>ObjectStore</code>.
	 *
	 * @param name The name of the object to remove.
	 * @throws IOException if there was a problem removing .the object from this store.
	 */	
	public void remove(String name) throws IOException {
		lock.lock();
		try{
			delegate.remove(name);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Puts a new <code>Encodeable</code> object with the associated name in this object store.
	 *
	 * @param <E> The type of the object to insert.
	 * @param name The name of the object to insert.
	 * @param e The object to insert.
	 * @throws IOException if there was a problem putting the object in this object store.
	 */	
	public <E extends Encodable> void put(String name, E e) throws IOException {
		lock.lock();
		Path path = null;
		Path bk = null;
		boolean success = false;
		try{
			path = delegate.dir.resolve(name);
			if(Files.exists(path)){
				bk = delegate.dir.resolve(name+".bk");
				Files.move(path, bk, REPLACE_EXISTING, ATOMIC_MOVE);
			}
		
			delegate.out(e.encode(), path);
			success = true;
		}catch(IOException e2){
			if(bk != null){
				try{
					Files.move(bk, path, REPLACE_EXISTING, ATOMIC_MOVE);
				}catch(IOException e3){
					throw new IOException("Operation failed, couldn't roll back" ,e3);
				}
			}
			throw new IOException("Operation failed, rolled back", e2);
		}finally{
			if(success && bk != null && Files.exists(bk)){
				try{
					Files.delete(bk);
				}catch(IOException e1){}
			}
			lock.unlock();
		}
	}

	/**
	 * Returns the object associated with the given name from this object store.
	 *
	 * @param name The name of the object to get.
	 * @return The object associated with the given name from this object store or null if there is no such object.
	 * @throws IOException if there was a problem getting the object.
	 * @throws NullPointerException if this object store was not initialized with a decode with associated 
	 * type <code>E</code>.
	 */	
	public E get(String name) throws IOException {
		lock.lock();
		try{
			return get(name, decoder);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Returns the object associated with the given path from this object store.
	 *
	 * @param path The path of the object to get.
	 * @return The object associated with the given path from this object store or null if there is no such object.
	 * @throws IOException if there was a problem getting the object.
	 * @throws NullPointerException if this object store was not initialized with a decode with associated 
	 * type <code>E</code>.
	 */	
	protected E get(Path path) throws IOException {
		return get(path, decoder);
	}
	
	/**
	 * Returns the object associated with the given name and given <code>Decoder</code> from this object store.
	 *
	 * @param <S> The type of the object to get.
	 * @param name The name of the object to get.
	 * @param decoder The decoder of the object to get.
	 * @return Returns the object associated with the given name and given <code>Decoder</code> from this object store.
	 * @throws IOException if there was a problem getting the object.
	 */	
	public <S> S get(String name, Decoder<S> decoder) throws IOException {
		lock.lock();
		try{
			return get(delegate.dir.resolve(name), decoder);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the object associated with the given path and given <code>Decoder</code> from this object store.
	 *
	 * @param <S> The type of the object to get.
	 * @param path The path of the object to get.
	 * @param decoder The decoder of the object to get.
	 * @return Returns the object associated with the given path and given <code>Decoder</code> from this object store.
	 * @throws IOException if there was a problem getting the object.
	 */	
	protected <S> S get(Path path, Decoder<S> decoder) throws IOException {
		if(!Files.exists(path)) return null;
		try(VarInputStream in = delegate.in(path)) {
			return decoder.read(in);
		}
	}
}