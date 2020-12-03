# Kiribi-Store
Kiribi NOSQL module

### Introduction
Lightweight NOSQL store for encodable objects.

### Features
* Lightweight NOSQL store for encodable objects.

### Overview
Lightweight NOSQL store for encodable objects.

##### Object Store
Stores encodable objects.

##### List Store
Stores a list of encodable objects.

### Code Example

	Path root = ...
	String schema = ...
	StoreDelegate delegate = new StoreDelegate(root, schema);
	ObjectStore store = new ObjectStore(delegate, null);
	
	String name = "foo";   	   
	Foo foo = new Foo();
	store.put(name, foo);
	foo = store.get(name, Foo::new);

### Module Dependencies
##### Requires
* java.base
* rs.igram.kiribi.io

##### Exports
* rs.igram.kiribi.store

### Status
* Experimental.

### To Do
* Determine minimum supported Java version.
* Finish unit tests.

### Known Issues
* Must call list() method on ListStore to load.
