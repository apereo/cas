package com.authy.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * Generic class to instance a response from the API
 * @author Julian Camargo
 *
 */

public class Instance {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    Integer status;
    String content;
    Error error;

    public Instance() {
        content = "";
    }

    public Instance(int status, String content) {
        this.status = status;
        this.content = content;
    }

    /**
     * Check if this is instance is correct. (i.e No error occurred)
     * @return true if no error occurred else false.
     */
    public boolean isOk() {
        return status == 200;
    }

    /**
     * Return an Error object with the error that have occurred or null.
     * @return an Error object
     */
    public Error getError() {
        if(isOk())
            return error;

        try {
            JAXBContext context = JAXBContext.newInstance(Error.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            StringReader xml = new StringReader(content);
            if(!content.isEmpty())
                error = (Error)unmarshaller.unmarshal(new StreamSource(xml));
        }
        catch(JAXBException e) {
            logger.error(e.getMessage(), e);
        }

        return error;
    }

    public void setStatus(int s) {
        if (this.status == null)
            this.status = s;
    }

    /**
     * Set an Error object.
     * @param error the error
     */
    public void setError(Error error) {
        this.error = error;
    }
}

