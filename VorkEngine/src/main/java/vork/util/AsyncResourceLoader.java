package vork.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AsyncResourceLoader implements Runnable {

	private Thread thread;
	
	@FunctionalInterface
	public interface ThrowableSupplier {
		Object get(FilePath file) throws Exception;
	}
	
	@FunctionalInterface
	public interface ThrowableIteratorSupplier {
		Object get(FilePath file) throws Exception;
	}
	
	@AllArgsConstructor
	private class AsyncResource {
		private ThrowableSupplier supplier;
		private FilePath file;
		private Consumer<LoadedAsyncResource> onProcessed;
		
	}
	
	@AllArgsConstructor
	private class IteratableAsynResource {
		private Iterator<FilePath> iterator;
		private ThrowableIteratorSupplier supplier;
		private Consumer<LoadedAsyncResource> onProcessed;
	}
	
	@AllArgsConstructor
	public class LoadedAsyncResource {
		@Getter
		private Object loadedObject;
		@Getter
		private FilePath file;
		private Consumer<LoadedAsyncResource> onProcessed;
	}
	
	private Queue<Object> asyncResources = new LinkedList<>();
	
	/**
	 * If an exception is encountered when loading a resource on
	 * the resource thread it is placed here. Then it can be accepted
	 * by the main thread and handled appropriately.
	 */
	private Queue<Exception> exceptionsEncountered = new ConcurrentLinkedQueue<>();
	
	/**
	 * When the object has finished loading on the resource thread
	 * it is placed here to be handled on the main thread.
	 */
	private Queue<LoadedAsyncResource> loadedResources = new ConcurrentLinkedQueue<>();
	
	public void start() {
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		try {
			if (thread != null) {
				thread.interrupt();
				thread.join();	
				thread = null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void load(FilePath file, ThrowableSupplier supplier, Consumer<LoadedAsyncResource> onProcessed) {
		asyncResources.add(new AsyncResource(supplier, file, onProcessed));
	}
	
	public <E> void loadDirectory(FilePath directoryPath, ThrowableIteratorSupplier supplier,
			                      Consumer<LoadedAsyncResource> onProcessed) throws IOException {
		asyncResources.add(new IteratableAsynResource(directoryPath.getDirectoryIterator(), supplier, onProcessed));
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted() && !asyncResources.isEmpty()) {
			Object resourceObject = asyncResources.poll();
			try {
				if (resourceObject instanceof AsyncResource) {
					AsyncResource resource = ((AsyncResource) resourceObject);
					Object loadedResource = resource.supplier.get(resource.file);
					loadedResources.add(new LoadedAsyncResource(loadedResource, resource.file, resource.onProcessed));
				} else {
					IteratableAsynResource iterableResource = (IteratableAsynResource) resourceObject;
					Iterator<FilePath> iterator = iterableResource.iterator;
					while (iterator.hasNext() && !Thread.currentThread().isInterrupted()) {
						ThrowableIteratorSupplier supplier = iterableResource.supplier;
						FilePath file = iterableResource.iterator.next();
						Object loadedResource = supplier.get(file);
						loadedResources.add(new LoadedAsyncResource(loadedResource, file, iterableResource.onProcessed));
					}
				}
				
			} catch (Exception e) {
				exceptionsEncountered.add(e);
			}
		}
	}
	
	public Exception getPossibleEncounteredException() {
		return exceptionsEncountered.poll();	
	}
	
	public void process() {
		if (!loadedResources.isEmpty()) {
			LoadedAsyncResource resource = loadedResources.poll();
			resource.onProcessed.accept(resource);	
		}
	}

	public boolean isFinished() {
		return !thread.isAlive() && loadedResources.isEmpty();
	}
}
