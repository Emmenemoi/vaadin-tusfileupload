package com.asaoweb.vaadin.tusfileupload;

public interface Locker
{
	/* 
		Returns true if able to lock name without waiting.  False if
		name is already locked.  
	*/
	public boolean lockUpload(String name) throws Exception;

	/* Unlocks name */
	public void unlockUpload(String name) throws Exception;
}