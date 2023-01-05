select id, matricula, tipo, modelo, marca, ano, proprietario, count(id) as nrviagens
from viagem v left join veiculo ve on v.veiculo = ve.id
where ano <= 2018
group by (id, matricula , tipo, modelo, marca, ano, proprietario)
order by id;

select latinicio, longinicio, latfim, longfim
from viagem
where veiculo = 2;

SELECT * from pessoa;

SELECT * from viagem;

SELECT * from veiculo;

SELECT * from tipoveiculo;

SELECT * from veiculo_old;

SELECT * FROM periodoactivo;

update veiculo
set ano = 2017
where id = 2;

delete from veiculo_old;

drop table veiculo_old;

update viagem 
set hfim = '6:55'
where idsistema = 5;

select id, matricula, tipo, modelo, marca, ano, proprietario, idsistema
from viagem v left join veiculo ve on v.veiculo = ve.id
order by id;

select id, matricula, tipo, modelo, marca, ano, proprietario, count(id) as nrviagens
from viagem v right join veiculo ve on v.veiculo = ve.id
where matricula = '11AA11'
group by (id, matricula , tipo, modelo, marca, ano, proprietario)

select sum(valfinal) from viagem where veiculo = 2;

select * from proprietario

select idpessoa, count(idpessoa)
from veiculo left join proprietario on veiculo.proprietario = proprietario.idpessoa
group by (idpessoa)