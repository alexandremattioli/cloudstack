package org.apache.cloudstack.vnf;

public interface VnfDictionaryManager {
    /**
     * Load and parse a YAML dictionary
     */
    VnfDictionary parseDictionary(String yaml)
        throws DictionaryParseException;
    /**
     * Validate dictionary structure and contents
     */
    DictionaryValidationResult validateDictionary(VnfDictionary dictionary);
    /**
     * Store dictionary in database
     */
    VnfDictionary storeDictionary(VnfDictionary dictionary, Long templateId, Long networkId)
        throws CloudException;
    /**
     * Get dictionary for template or network
     */
    VnfDictionary getDictionary(Long templateId, Long networkId);
    /**
     * Delete dictionary
     */
    boolean deleteDictionary(String uuid);
}
/**
 * Request builder that translates CloudStack operations to VNF commands
 */