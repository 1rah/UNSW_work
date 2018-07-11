## import modules here 
import pandas as pd
from collections import defaultdict


################# Question 1 #################

def multinomial_nb(training_data, sms):# do not change the heading of the function
    
    def get_count(token_list):
        d = defaultdict(float)
        for t in token_list:
            d[t]+=1
        return(d)
    
#    a = {1:1, 2:1}
#    b = {1:1, 3:1}
#    add_data_by_key(a,b)
#    print(a)
#    
    
    def add_data_by_key(a, b):
        for k in b.keys():
            if k in a:
                a[k] += b[k]
            else:
                a.update({k:b[k]})
        return
    
    def get_count_by_class(data = training_data):
        d = dict()
        for row in data:
            (row_count, row_class) = row
            if row_class in d:
                add_data_by_key(d[row_class], row_count)
            else:
                d[row_class] = row_count
        return d


    def get_tok_prob(t, count, vocab_len):
        
        if t in count:
            num = count[t] + 1
        else:
            num = 1
            
        den = sum(count.values()) + vocab_len
        
#        print(num, den)
        return(num, den)   


    ###run###
    
    #get dict by class, with a dict of word counts
    count_by_class = get_count_by_class()
    
    #get the set of vocab for all classes (spam and ham)
    vocab = set()
    for count in count_by_class.values():
#        print(count)
        for word in count.keys():
#            print(word)
            vocab.add(word)
#    print(vocab)

    # get input token counts
    tok_count = get_count(sms)
    
    #set priors, by each class, (no lines for class / total lines)
    prob = dict()
    n_total_entries = len(training_data)
    
    for class_name in count_by_class.keys():
        n_entries_by_class = len([r for r in training_data if r[1] == class_name])
        prob[class_name] = n_entries_by_class / n_total_entries
#        prob[class_name] = 1
#        print(class_name, prob, (n_entries_by_class, n_total_entries))
        
    
    for tok in tok_count.keys():
#        print(tok)
        for class_key in count_by_class.keys():
#            print(class_key)
            if tok in vocab:
                p = get_tok_prob(tok, count_by_class[class_key], len(vocab))
    #            print(p)
                prob_tok = (p[0] / p[1])**tok_count[tok]
                
                prob[class_key] *= prob_tok
            
    return prob['spam'] / prob['ham']

    
######################

if __name__ == "__main__":
         
    def tokenize(sms):
        return sms.split(' ')
    
    def get_freq_of_tokens(sms):
        tokens = {}
        for token in tokenize(sms):
            if token not in tokens:
                tokens[token] = 1
            else:
                tokens[token] += 1
        return tokens
    
    
    raw_data = pd.read_csv('./asset/data.txt', sep='\t')
    raw_data.head()
    
    
    training_data = []
    for index in range(len(raw_data)):
        training_data.append((get_freq_of_tokens(raw_data.iloc[index].text), raw_data.iloc[index].category))
        
    sms = 'I am not spam'
    #sms = 'Chinese Chinese Chinese Tokyo Japan'
    test = multinomial_nb(training_data, tokenize(sms))
    print(test)