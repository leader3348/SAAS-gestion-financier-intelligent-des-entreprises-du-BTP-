-- Données de référence commerciales (hypothèses de prix à valider avec de vrais clients). Pas de données de démo ici.
INSERT INTO plans (code,name,price_monthly,price_yearly,max_users,max_active_projects,features) VALUES
('STARTER','Starter',149,1490,3,3,'clients,prestations,facturation,dashboard_base'),
('PRO','Pro',299,2990,10,15,'clients,prestations,facturation,dashboard_base,workflows,automatisations,rapports,documents'),
('BUSINESS','Business',599,5990,30,100,'tout_pro,analytics_avances,permissions_avancees,exports,parametrage_avance');
