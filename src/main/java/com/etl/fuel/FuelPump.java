package com.etl.fuel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.storage.jpa.Enums.FileType;

/**
 * The {@code FuelPump} class is responsible for managing different types of {@link Fuel} handlers
 * based on the provided {@link FileType}. It provides a way to retrieve the appropriate handler
 * for a given file type.
 *
 * @Component Indicates that this class is a Spring component and should be automatically
 * discovered and registered as a Spring bean during component scanning.
 */
@Component
public class FuelPump {

    /**
     * A mapping of {@link FileType} to corresponding {@link Fuel} handlers.
     */
    private final Map<FileType, Fuel> handlerMap;

    /**
     * Constructs a new {@code FuelPump} instance with the provided list of {@link Fuel} handlers.
     *
     * @param handlers A list of {@link Fuel} handlers to be used for mapping file types.
     */
    public FuelPump(List<Fuel> handlers) {
        // Create a mapping of FileType to Fuel using a stream and Collectors.toMap
        handlerMap = handlers.stream().collect(Collectors.toMap(Fuel::getFuelName, Function.identity()));
    }

    /**
     * Gets the {@link Fuel} handler associated with the specified {@link FileType}.
     *
     * @param fileType The {@link FileType} for which to retrieve the handler.
     * @return The corresponding {@link Fuel} handler or {@code null} if not found.
     */
    public Fuel getHandler(String fileType) {
    	FileType fileEnum = FileType.valueOf(fileType);
        return handlerMap.get(fileEnum);
    }
}
