package vork.server.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;

public class CommandManager {

	private static final Pattern SEGMENTED_COMMAND_PATTERN = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
	
	@AllArgsConstructor
	private class CommandId {
		private String name;
		private int numArguments;
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof CommandId)) return false;
			CommandId id = (CommandId) o;
			return id.name.equals(name) && id.numArguments == numArguments;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(name.hashCode(), numArguments);
		}
	}
	
	@AllArgsConstructor
	private class DispatchInstance {
		private Object listener;
		private Method method;
	}
	
	private static final Map<CommandId, DispatchInstance> commandMethods = new HashMap<>();
	
	@AllArgsConstructor
	private class QueuedDispatch {
		private DispatchInstance instance;
		private String command;
		boolean fixedArgsDispatch = false;
	}
	
	private final ArrayBlockingQueue<QueuedDispatch> dispatchQueue = new ArrayBlockingQueue<>(20);
	
	private String[] splitCommand(String command) {
		List<String> cmdAndArgsList = new ArrayList<String>();
		Matcher segmentMatcher = SEGMENTED_COMMAND_PATTERN.matcher(command);
		while (segmentMatcher.find()) {
			cmdAndArgsList.add(segmentMatcher.group());
		}
		String[] cmdAndArgs = new String[cmdAndArgsList.size()];
		return cmdAndArgsList.toArray(cmdAndArgs);
	}
	
	public void registerListener(Object listener) {
		Method[] methods = listener.getClass().getMethods();
		for (Method method : methods) {
			Command[] commandAnnos = method.getAnnotationsByType(Command.class);
			if (commandAnnos.length > 1) {
				throw new IllegalStateException("A method cannot have more than one command annotation: " + listener);
			} else if (commandAnnos.length == 0) {
				continue;
			}
			Command commandAnno = commandAnnos[0];
			
			String name = commandAnno.cmd();
			int numArgs = method.getParameterCount();
			if (numArgs == 1) {
				// an array of strings means that the method excepts any number of arguments
				if (method.getParameterTypes()[0].equals(String[].class)) {
					numArgs = -1;
				}
			}
			
			if (numArgs != -1) {
				// Validating that we can actually use the
				// argument type
				for (Class<?> paramType : method.getParameterTypes()) {
					if (!(paramType.equals(String.class) ||
						  paramType.equals(float.class) ||
						  paramType.equals(int.class))) {
						throw new IllegalStateException("command cannot take as an arugment type: " + paramType);
					}
				}
			}
			
			CommandId commandId = new CommandId(name, numArgs);
			if (commandMethods.containsKey(commandId)) {
				throw new IllegalStateException("command already exist with name: " + name + " and " + numArgs + " arguments");
			}
			
			commandMethods.put(commandId, new DispatchInstance(listener, method));
		}
	}
	
	public boolean dispatch(String command) {
		if (command.isEmpty()) return false;
		if (dispatchQueue.remainingCapacity() == 0) {
			System.out.println("dispatch queue is full");
			return true;
		}
		
		String[] cmdAndArgs = splitCommand(command);
		
		boolean fixedArgsDispatch = false;
		DispatchInstance instance = commandMethods.get(new CommandId(cmdAndArgs[0], cmdAndArgs.length - 1));
		if (instance == null) {
			instance = commandMethods.get(new CommandId(cmdAndArgs[0], -1));
		} else {
			fixedArgsDispatch = true;
		}
		if (instance == null) {
			return false;
		}
		
		dispatchQueue.add(new QueuedDispatch(instance, command, fixedArgsDispatch));
		return true;
	}
	
	public void processDispatches() {
		while (!dispatchQueue.isEmpty()) {
			QueuedDispatch queuedDispatch = dispatchQueue.poll();
			String[] cmdAndArgs = splitCommand(queuedDispatch.command);
			DispatchInstance instance = queuedDispatch.instance;
			
			try {
				if (queuedDispatch.fixedArgsDispatch) {
					Object[] dispatchArgs = new Object[cmdAndArgs.length - 1];
					Class<?>[] paramTypes = instance.method.getParameterTypes();
					for (int i = 1; i < cmdAndArgs.length; i++) {
						String arg = cmdAndArgs[i];
						if (paramTypes[i-1].equals(String.class)) {
							if (arg.length() > 2 &&
								arg.charAt(0) == '"' &&
								arg.charAt(arg.length()-1) == '"') {
								dispatchArgs[i-1] = arg.substring(1).substring(0, arg.length()-2);
							} else {
								dispatchArgs[i-1] = arg;	
							}
						} else if (paramTypes[i-1].equals(int.class)) {
							try {
								dispatchArgs[i-1] = Integer.parseInt(arg);	
							} catch (NumberFormatException e) {
								// TODO: !!!
							}
						} else if (paramTypes[i-1].equals(float.class)) {
							try {
								dispatchArgs[i-1] = Float.parseFloat(arg);	
							} catch (NumberFormatException e) {
								// TODO: !!!
							}
						}
					}
					instance.method.invoke(instance.listener, dispatchArgs);
				} else {
					String[] dispatchArgs = new String[cmdAndArgs.length - 1];
					System.arraycopy(cmdAndArgs, 1, dispatchArgs, 0, cmdAndArgs.length - 1);
					instance.method.invoke(instance.listener, (Object[]) dispatchArgs);		
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
