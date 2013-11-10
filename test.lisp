(define member? 
 (lambda (item lst) 
 (cond 
 ((null? lst) #f) 
 ((equal? item (car lst)) #t) 
 (else (member? item (cdr lst))) 
))) 
 
(member? 3 (quote (1 2 3))) 
(member? (quote b) (quote (a (b c) d)))