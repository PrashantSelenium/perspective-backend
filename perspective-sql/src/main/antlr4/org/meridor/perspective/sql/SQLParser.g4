
parser grammar SQLParser;

options
   { tokenVocab = SQLLexer; }

query
   : select_query
   | show_tables_query
   ;

//Entities
project_name
   : ID
   ;
   
table_name
   : ID
   ;
   
alias
   : ID
   ;
   
column_name
   : ( project_name DOT )? ID 
   | ( alias DOT )? ID
   ;
   
   
// Select query
select_query
   : select_clause ( from_clause )? ( where_clause )? ( group_clause )? ( having_clause )? ( order_clause )? ( limit_clause )? ( SEMICOLON )?
   ;

subquery
   : LPAREN select_query RPAREN
   ;
   
select_clause
   : SELECT select_expression
   ;

from_clause
   : FROM table_references
   ;

where_clause
   : WHERE boolean_expression
   ;
   
having_clause
   : HAVING boolean_expression
   ;

group_clause
   : GROUP BY group_by_expression
   ;

order_clause
   : ORDER BY columns_list
   ;

offset : INT;

row_count: INT;

limit_clause
   : LIMIT ( ( offset COMMA )? row_count | row_count OFFSET offset )
   ;

alias_clause
   : ( AS )? alias
   ;

aliased_element
   : element ( alias_clause )?
   ;
   
select_expression
   : aliased_element ( COMMA aliased_element )*
   ;
   
group_by_element
   : column_name 
   | function_call
   ;
   
group_by_expression
   : group_by_element ( COMMA group_by_element )*
   ;
   
columns_list
   : column_name ( COMMA column_name )*
   ;

boolean_expression
   : simple_boolean_expression ( boolean_operation simple_boolean_expression )*
   ;

function_name
   : COUNT
   ;

function_args
   : element ( COMMA element )*
   ;
   
function_call
   : function_name LPAREN function_args RPAREN
   ;

element
   : STRING 
   | INT 
   | column_name 
   | function_call 
   | subquery
   ;

relational_operation
   : EQ 
   | LT 
   | GT 
   | NOT_EQ 
   | LTE 
   | GTE
   ;

boolean_operation
   : AND 
   | XOR 
   | OR 
   | NOT
   ;

between_op
   : BETWEEN
   ;

is_or_is_not
   : IS | IS NOT
   ;

simple_boolean_expression
   : element relational_operation element 
   | element between_op element AND element 
   | element is_or_is_not NULL
   ;

table_references
   : table_reference ( ( COMMA table_reference ) | join_clause )*
   ;

table_reference
   : general_join 
   | table_atom
   ;

general_join
   : complex_join ( ( INNER | CROSS )? JOIN table_atom ( join_condition )? )?
   ;

complex_join
   : medium_join ( STRAIGHT_JOIN table_atom ( ON boolean_expression )? )?
   ;

medium_join
   : simple_join ( ( LEFT | RIGHT ) ( OUTER )? JOIN simple_join join_condition )?
   ;

simple_join
   : table_atom ( NATURAL ( ( LEFT | RIGHT ) ( OUTER )? )? JOIN table_atom )?
   ;

table_atom
   : table_name ( alias_clause )? 
   | subquery alias_clause 
   | LPAREN table_references RPAREN
   ;

join_clause
   : ( INNER | CROSS )? JOIN table_atom ( join_condition )? 
   | STRAIGHT_JOIN table_atom ( ON boolean_expression )?
   | ( LEFT | RIGHT ) ( OUTER )? JOIN simple_join join_condition 
   | NATURAL ( ( LEFT | RIGHT ) ( OUTER )? )? JOIN table_atom
   ;

join_condition
   : ON boolean_expression ( boolean_operation boolean_expression )* 
   | USING LPAREN columns_list RPAREN
   ;


// Show tables query
show_tables_query
   : SHOW_TABLES
   ;