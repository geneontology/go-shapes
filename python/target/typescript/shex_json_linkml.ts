




/**
 * GO domain/range constraint shape is defined as the domain, relationship, and range of a GO shape  expression rule
 */

export interface GoShape  {
    
    
    /**
     * The domain of the GO shape expression rule, this is the subject of the relationship.
     */
    domain_name?: string,
    
    
    /**
     * A list of relationships that are defined for a particular domain
     */
    relationships?: Relationship[],
    
}


/**
 * A relationship object holds the relationship (Relationship.id) between a GoShape.domain (subject) and the Relatioship.range (object),  the range of the relationship (what values can be provided in the object of a statement), whether or not the  object can be multivalued, if the object is required when used with a particular domain and what context the  relationship should be used for.
 */

export interface Relationship  {
    
    
    /**
     * The relationship id is the predicate of the relationship between the domain and  range of the GO shape expression rule.
     */
    id?: string,
    
    
    /**
     * The range of the relationship identified by the Relationship.id parameter (This contains the values can be provided in the object of a statement)
     */
    range?: string,
    
    
    /**
     * for this shape, the relationship in question supports multiple values in the object of the association.
     */
    is_multivalued?: boolean,
    
    
    /**
     * None
     */
    is_required?: boolean,
    
    
    /**
     * used to determine if this shape is used in the the visual pathway editor or the graphical editor.  Those shapes annotated with like this https://github.com/geneontology/go-shapes/pull/285/files will be exlcuded from the visual pathway editor but still included in the file so this file can be used in the graphical editor as well.
     */
    context?: string,
    
}


/**
 * A collection of GO domain/range constraint shapes.  This is primarily used in this schema to allow several  test data objects to be submitted in a single file. 
 */

export interface Collection  {
    
    
    /**
     * A collectionm of GO domain/range constraint shapes where a GO domain/range constraint shape  is defined as the domain, relationship, and range of a GO shape expression rule.
     */
    goshapes?: GoShape[],
    
}

