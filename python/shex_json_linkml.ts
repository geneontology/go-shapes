




/**
 * A generic grouping for any identifiable entity
 */

export interface GoShape  {
    
    
    /**
     * None
     */
    domain_name?: string,
    
    
    /**
     * None
     */
    relationships?: Relationship[],
    
}


/**
 * A relationship between two entities
 */

export interface Relationship  {
    
    
    /**
     * None
     */
    id?: string,
    
    
    /**
     * None
     */
    range?: string,
    
    
    /**
     * None
     */
    is_multivalued?: boolean,
    
}


/**
 * None
 */

export interface Collection  {
    
    
    /**
     * None
     */
    goshapes?: GoShape[],
    
}

