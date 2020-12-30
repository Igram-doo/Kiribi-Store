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

Java version 11

