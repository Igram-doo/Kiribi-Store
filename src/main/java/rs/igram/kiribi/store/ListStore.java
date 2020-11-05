
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
import java.io.UncheckedIOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Array;  
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import rs.igram.kiribi.io.Decoder;
import rs.igram.kiribi.io.Encodable;

import static rs.igram.kiribi.store.ListStore.Type.*;

/**
 * An instance of this class manages persistent storage of a list of <code>Encodable</code> elements.
 *
 * @author Michael Sargent
 */
public class ListStore<E extends Encodable> extends ObjectStore<E> {
	
	/**
	 * The "namer" function which returns a unique string associated with the provided
	 * <code>E</code> instance.
	 */
	protected final Function<E, String> namer;
	
	/**
	 * The class of <code>E[]</code>.
	 */
	protected final Class<E[]> clazz;
	
	/**
	 * The <code>List</code> managed by this list store.
	 */
	protected List<E> list;

	/**
	 * Initializes a newly created <code>ListStore</code> object.
	 *
	 * @param clazz The class of <code>E[]</code>.
	 * @param decoder The <code>Decoder</code> for elements of this list store.
	 * @param namer The "namer" function for this list store.
	 * @param delegate The <code>StoreDelege</code> for this list store.
	 * @throws IOException if there was a problem instantiating an instance of this class.
	 */
	public ListStore(Class<E[]> clazz, Decoder<E> decoder, Function<E, String> namer, StoreDelegate delegate) throws IOException {
		super(delegate, decoder);
		this.namer = namer;
		this.clazz = clazz;
	}
	
	/**
	 * Returns the <code>List</code> associated with this list store.
	 *
	 * @return Returns the <code>List</code> associated with this list store.
	 * @throws IOException if this list store was not previously loaded and there was a problem 
	 * loading this list store from memory.
	 */	
	public List<E> list() throws IOException {
		if(list == null){
			list = new ArrayList<E>();
			load(list);
		}
		
		return list;
	}
	
	/**
	 * Returns the name of the object determined by the namer this list store was initialized with.
	 *
	 * @param t The object to name.
	 * @return Returns the name of the object determined by the namer this list store was initialized with.
	 */	
	public String name(E t) {return namer.apply(t);}
	
	/**
	 * Replaces on object in this list store.
	 *
	 * @param t The object to replace.
	 * @throws IOException if there was a problem replacing the given object in this list store.
	 */	
	public void replace(E t) throws IOException {
		String name = name(t);
		E old = get(name);
		put(name, t);
		if(old != null) list.remove(old);
		list.add(t);
		E[] data = array(1);
		data[0] = t;
		notify(UPDATED, data);
	}
	
	/**
	 * Loads this list store from memory.
	 *
	 * @param list The list into which this <code>ListStore</code> will be loaded.
	 * @throws IOException if there was a problem loading this list store from memory.
	 */	
	protected void load(List<E> list) throws IOException {
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(delegate.dir)) {
           for(Path entry: stream) {
           	   if(!Files.isDirectory(entry)){
           	   	   E t = get(entry);
           	   	   if(t != null) list.add(t);
               }
           }
       	}catch(DirectoryIteratorException e) {
           throw new IOException(e);
       	}
	}
	
	/**
	 * Removes an array of objects from this list store.
	 *
	 * @param items The items to remove from this <code>ListStore</code>.
	 * @throws IOException if there was a problem removing the given objects from this list store.
	 */	
	public void remove(E... items) throws IOException {
		for(E t : items){
			remove(name(t));
			list().remove(t);
		}
		notify(DELETED, items);
	}
	
	/**
	 * Removes a collection of objects from this list store.
	 *
	 * @param items The items to remove from this <code>ListStore</code>.
	 * @throws IOException if there was a problem removing the given collection from this list store.
	 */	
	public void remove(Collection<? extends E> items) throws IOException {
		for(E t : items){
			remove(name(t));
			list().remove(t);
		}
		notify(DELETED, items.toArray(array(items.size())));
	}
	
	/**
	 * Adds an array of objects to this list store.
	 *
	 * @param items The items to add to this <code>ListStore</code>.
	 * @throws IOException if there was a problem adding the given objects to this list store.
	 */	
	public void add(E... items) throws IOException {
		for(E t : items){
			put(name(t), t);
			list().add(t);
		}
		notify(ADDED, items);
	}
	
	/**
	 * Adds a collection of objects to this list store.
	 *
	 * @param items The items to add to this <code>ListStore</code>.
	 * @throws IOException if there was a problem adding the given collection ton this list store.
	 */	
	public void add(Collection<? extends E> items) throws IOException {
		for(E t : items){
			put(name(t), t);
			list().add(t);
		}		
		notify(ADDED, items.toArray(array(items.size())));
	}
	
	/**
	 * Subclasses can override this method to process changes to this list store.
	 *
	 * @param type The type of change to this <code>ListStore</code>.
	 * @param data The data which changed.
	 */	
	protected void notify(ListStore.Type type, E[] data) {}
	
	private E[] array(int length) {
		return clazz.cast(Array.newInstance(clazz.getComponentType(), length));
	}
	
	/**
	 * Enum indicating the type of change which occurred.
	 */
	public static enum Type {
		/** Indicates items where added */
		ADDED,
		/** Indicates items where deleted */
		DELETED,
		/** Indicates items were updated */
		UPDATED,
		/** Indicates items were modified */
		MODIFIED;
	}
}