import { createContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null); 

    useEffect(() => {
        const storedToken = localStorage.getItem("token"); 
        if (storedToken) {
            setToken(storedToken);
            const info = JSON.parse(atob(storedToken.split('.')[1])); 
            setUser({ userId: info.id, userName: info.sub, role: info.role });
        } 
    }, [token]);

    return (
        <AuthContext.Provider value={{ token, user, setUser }}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthProvider;