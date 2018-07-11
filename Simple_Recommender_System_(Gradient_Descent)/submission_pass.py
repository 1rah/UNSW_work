import numpy as np


def output_dense_matrix(M):
    print('\n'.join([' '.join(['{:.6f}'.format(item) for item in row]) for row in M]))

################# Question 1 #################



def obj_fn(R, Q, P, _mu_1, _mu_2, _lambda_1, _lambda_2):
        
        ## Check the Error
        term_1 = term_2 = term_3 = 0
        
#        Rn = np.dot(Q, P.transpose())
        for  i, x, r in R:
            term_1 += (r - np.dot(P[x,:], Q[i,:]))**2
#            term_1 += (r - Rn[i,x])**2
        
#        for x in range(P.shape[0]):
#            term_2 += _lambda_1 * np.linalg.norm(P[x,:])**2
        term_2 =  _lambda_1 *np.linalg.norm(P)**2
        
        
#        for i in range(Q.shape[0]):
#            term_3 += _lambda_2 * np.linalg.norm(Q[i,:])**2
        term_3 = _lambda_2 * np.linalg.norm(Q)**2
            
        return (term_1 + term_2 + term_3)
        




def lf(R, Q, P, max_iter, _err, _mu_1, _mu_2, _lambda_1, _lambda_2, debug=True):# do not change the heading of the function

    iteration = 0
    
    val_obj_fn_1 = obj_fn(R, Q, P, _mu_1, _mu_2, _lambda_1, _lambda_2)
    debug or print(val_obj_fn_1, end=', ')
    
    while True:
        np.random.shuffle(R)
        
        for i, x, r in R:
            
#            Rn = np.dot(Q, P.transpose())
#            e = 2*(r - Rn[i,x])
            e = 2*(r - np.dot(P[x,:], Q[i,:]))
        
            nQ = Q[i,:] + _mu_1*((e*P[x,:]) - (2*_lambda_2*Q[i,:]))
            nP = P[x,:] + _mu_2*((e*Q[i,:]) - (2*_lambda_1*P[x,:]))
            
            Q[i,:], P[x,:] = nQ, nP
            
            val_obj_fn_2 = obj_fn(R, Q, P, _mu_1, _mu_2, _lambda_1, _lambda_2)
            debug or print(val_obj_fn_2, end=', ')
            
            delta_obj_fn = abs(val_obj_fn_1 - val_obj_fn_2)
            debug or print(delta_obj_fn)
            
            val_obj_fn_1 = val_obj_fn_2
            iteration += 1
        
            if iteration >= max_iter:
                debug or print('iteration max: ', iteration)
                return Q, P
        
            if delta_obj_fn <= _err:
                debug or print('converged: ')
                return Q, P
        

    
    return Q, P


if __name__ == "__main__":
    
    R_sparse = np.loadtxt('asset/data.txt', dtype = int)
    ran_generator = np.random.RandomState(24)
    num_fac = 99
    
    
    
    num_item, num_user  = 4, 4
    
    R = np.zeros((num_item, num_user))
    for item_id, user_id, rank in R_sparse:
        R[item_id, user_id] = rank
    
    Q = ran_generator.random_sample((num_item, num_fac))
    P = ran_generator.random_sample((num_user, num_fac))
    
    #         lf(R,     Q,  P, max_iter, _err, _mu_1, _mu_2, _lambda_1, _lambda_2)
    Qn, Pn = lf(R_sparse, Q, P, 5000, 0.0001, 0.01, 0.01, 0.001, 0.001, debug=False)
    
    Rn = np.dot(Qn, Pn.transpose())
    
    term_1=[]
    for  i, x, r in R_sparse:
#        term_1 += (r - Rn[i,x])**2
        term_1 += [(r - np.dot(Pn[x,:], Qn[i,:]))**2]
    print(sum(term_1)**0.5/len(term_1))
    
    

#np.dot(P[0,:],Q.transpose()[:,0])
#Out[44]: 0.39968380334812098
#
#np.dot(P[0,:],Q[0,:])
#Out[45]: 0.39968380334812098


#sum(q**2 for q in Q.reshape(1,-1).tolist()[0])
#Out[38]: 17.08159572952628

#np.linalg.norm(Q)**2
#Out[39]: 17.081595729526278

#np.sum(Q**2)
#Out[15]: 16.035475572366781