package com.avro.archieve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;

public abstract class ETLProcessor {

	public void processMessage(MessageData data, String path) throws Exception {

		try {
			byte[] document = readDocument(data.getRawFilePath());
			Schema avroSchema = loadAvroSchemaFromFile.get();
			List<MappingData> mappings = loadMappings();
			GenericRecord avroRecord = createAvroRecord(mappings, document, avroSchema);
			saveAvroFile(avroRecord, path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Read document
	protected byte[] readDocument(String filePath) throws IOException {
		// Create a Path object from the file path
		Path path = Paths.get(filePath);
		// Read the file content as a byte array
		return Files.readAllBytes(path);
	}

	// Read mappings
	protected abstract List<MappingData> loadMappings();

	// Create avro record for storing it in avro format.
	protected abstract GenericRecord createAvroRecord(List<MappingData> mappingData, byte[] document, Schema schema)
			throws Exception;

	// Save Avro
	protected void saveAvroFile(GenericRecord record, String path) throws Exception {
		OutputStream outputStream = new FileOutputStream(path);
		JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), outputStream);
		GenericDatumWriter<GenericRecord> dataFileWriter = new GenericDatumWriter<>(record.getSchema());
		dataFileWriter.write(record, encoder);
		encoder.flush();
	}

	private static Supplier<Schema> loadAvroSchemaFromFile = () -> {
		try {
			Parser parser = new Schema.Parser();
			return parser.parse(new File("src/main/resources/output.avsc"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	};
}
