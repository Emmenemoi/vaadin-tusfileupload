package com.asaoweb.vaadin.fileupload;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
Todo: 
	- how does jackson marshal fields with null values?
*/
/*
	- POST method writes this object to disk
	- HEAD and other methods read it from disk. 
		this.offset is not stored in serialized version of this struct, 
		instead it is set by stat'ing the bin file with this.id.
*/
public class FileInfo implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(FileInfo.class.getName());
	public long entityLength = -1;
	public String id;
	public long offset = -1;
	public long index;
	public String metadata;
	public String suggestedFilename;
	public String suggestedFiletype;
	public String username;
	public URI 		preview;
	public URI		uploadURL;
	public Map<String, String> decodedMetadata;
	
	@JsonIgnore
	public String queueId;
	
	// This ctor is used by post method to create the FileInfo
	public FileInfo(long entityLength, String metadata, String username) {
		this.entityLength = entityLength;
		this.id = UUID.randomUUID().toString();
		this.id = this.id.replace("-", "_");
		this.metadata = metadata;
		this.decodedMetadata = parseMetadata(metadata);
		this.username = username;

		// See if client sent suggested filename in metadata and log it.
		this.suggestedFilename = decodedMetadata.get("filename");
		this.suggestedFiletype = decodedMetadata.get("filetype");
		this.index = System.currentTimeMillis();
		log.debug("New file ID = {}, filename={}, queueId={}, username={}", id, suggestedFilename, queueId, username);
	}
	public FileInfo(String queueId, long entityLength, long offset, String filename, String filetype) {
		super();
		this.entityLength = entityLength;
		this.queueId = queueId;
		this.id = queueId;
		this.offset = offset;
		this.suggestedFilename = filename;
		this.suggestedFiletype = filetype;
		this.index = System.currentTimeMillis();
	}

	// This is used by jackson to deserialize from file
	public FileInfo() {
	}

	@JsonIgnore
	public boolean isFinished() {
		return entityLength == -1 || (entityLength > 0 && entityLength == offset);
	}
	
	@JsonIgnore
	public boolean isQueued() {
		return entityLength > 0 && offset < entityLength ;
	}
	
	@JsonIgnore
	public boolean isUploading() {
		return entityLength > 0 && offset < entityLength && offset > 0 ;
	}

	public long getIndex() {
		return index;
	}

	public URI getPreview() {
		return preview;
	}

	public void setPreview(URI preview) {
		this.preview = preview;
	}

	public URI getUploadURL() {
		return uploadURL;
	}

	public void setUploadURL(URI uploadURL) {
		this.uploadURL = uploadURL;
	}

	/*
	 * Metadata is transmitted as comma separated key/value pairs, where key and
	 * value are separated by a space and value is base64 encoded. TODO: not sure if
	 * it's url safe, mime safe or std base64. Currently using url safe.
	 */
	Map<String, String> parseMetadata(String metadata) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (metadata == null) {
			return map;
		}
		String[] pairs = metadata.split(",");
		for (int i = 0; i < pairs.length; i++) {
			String[] element = pairs[i].trim().split(" ");
			if (element.length != 2) {
				log.warn("Ignoring metadata element:'" + pairs[i] + "'");
				continue;
			}
			String key = element[0];
			byte[] value;
			try {
				value = Base64.getUrlDecoder().decode(element[1]);
			} catch (IllegalArgumentException iae) {
				log.warn("Invalid encoding of metadata element:'" + pairs[i] + "'");
				continue;
			}
			map.put(key, new String(value));
		}
		return map;
	}

	@Override
	public String toString() {
		return String.format("FileInfo(%s) - name: %s, type: %s, queueId: %s, length: %d, offset: %d, uploadURL: %s",
			id, suggestedFilename, suggestedFiletype, queueId, entityLength, offset, uploadURL );
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FileInfo)) return false;

		FileInfo fileInfo = (FileInfo) o;

		return id.equals(fileInfo.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}