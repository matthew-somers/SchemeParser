(define member? 
 (lambda (item lst) 
 (cond 
 ((null? lst) #f) 
 ((equal? item (car lst)) #t) 
 (else (member? item (cdr lst))) 
))) 
 
(member? 3 '(1 2 3))
(member? 'b '(a (b c) d))
(define remove-last 
 (lambda (item lst) 
 (cond 
 ((null? lst) '()) 
 ((and (equal? item (car lst)) (not (member? item (cdr lst)))) (cdr lst)) 
 (else (cons (car lst) (remove-last item (cdr lst))))) 
)) 
 
(remove-last 'a '(b a n a n a s))
(remove-last '(a b) '(a b (a b) a b (b a) a b (a b) a b)) 
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
 
(define sandwich-first 
 (lambda (a b lst) 
 (cond 
 ((null? lst) '()) 
 ((null? (cdr lst)) lst) 
 ((and (equal? b (car lst)) (equal? b (cadr lst))) 
 (append (list b a b) (cddr lst))) 
 (else (cons (car lst) (sandwich-first a b (cdr lst))))) 
)) 
 
(sandwich-first 'meat 'bread '(bread bread))
(sandwich-first 'meat 'bread '())
(sandwich-first 'meat 'bread '(meat meat))