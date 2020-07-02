package script.utils;

import chat.errors.CoreException;

public interface ShutdownListener {
    public void shutdown() throws CoreException;
}
