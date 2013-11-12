(define same-structure? 
 (lambda (x y) 
 (cond 
 ((and (null? x) (null? y)) #t) 
 ((null? x) #f) 
 ((null? y) #f) 
 ((and (pair? (car x)) (pair? (car y))) 
 (and (same-structure? (car x) (car y)) 
 (same-structure? (cdr x) (cdr y)))) 
 (else (and (same-type? (car x) (car y)) 
 (same-structure? (cdr x) (cdr y))))) 
)) 
 
(define float? 
 (lambda (x) 
 (and (real? x) (not (integer? x))) 
)) 
 
(define same-type? 
 (lambda (x y) 
 (or (and (symbol? x) (symbol? y)) 
 (and (integer? x) (integer? y)) 
 (and (float? x) (float? y)) 
 (and (boolean? x) (boolean? y)) 
 (and (char? x) (char? y)) 
 (and (string? x) (string? y))) 
)) 
 
(same-structure? '(1 (a (b 3.14) ((c)))) '(3 (z (x 1.23) ((q)))))
(same-structure? '(1 (a (b 3.14) ((c)))) '(3 (z (x 3) ((q)))))
(same-structure? '(1 2 3 4 5) '(5 4 3 2))
(same-structure? '() '())
(same-structure? '(("hello") "world") '(("good-bye") "sam"))
